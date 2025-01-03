package org.task.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.task.dto.LisInfoDto;
import org.task.dto.LisTokenDto;
import org.task.dto.PricesAnswerDto;
import org.task.entities.LisInfoEntity;
import org.task.repositories.LisInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.task.services.parsers.TmPriceParser;
import org.task.util.HttpClientService;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@EnableAsync
public class MarketControlService implements InfoServiceInterface {
    private static final Logger log = LoggerFactory.getLogger(InfoServiceInterface.class);
    private final LisInfoRepository lisInfoRepository;
    private final MarketService marketService;
    private final CloseableHttpClient httpClient;
    private final TmPriceParser tmPriceParser;

    public MarketControlService(
            @Autowired HttpClientService httpClientService,
            @Autowired LisInfoRepository lisInfoRepository,
            @Autowired MarketService marketService,
            @Autowired TmPriceParser tmPriceParser
    ) {
        this.httpClient = httpClientService.getHttpClient();
        this.lisInfoRepository = lisInfoRepository;
        this.marketService = marketService;
        this.tmPriceParser = tmPriceParser;
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void updateStats() {
        log.info("TM parsing prices started");
        setPriceMap();
    }

    /**
     * Покупает предмет на рынке, используя переданный токен и имя предмета.
     * Ищет информацию о предмете по имени в базе данных, а затем пытается выполнить покупку через {@link MarketService#buyItem}.
     * Возвращает {@code true}, если покупка успешно выполнена, и {@code false}, если предмет не найден или не удалось выполнить покупку.
     *
     * @param lisTokenDto токен, необходимый для авторизации при совершении покупки
     * @param itemName имя предмета, который нужно купить
     * @return {@code true}, если предмет был найден и покупка была успешно совершена, {@code false} в противном случае
     */
    public boolean buyItem(LisTokenDto lisTokenDto, String itemName) {
        Optional<LisInfoEntity> optionalItem = lisInfoRepository.findByName(itemName);
        return optionalItem
                .filter(lisInfoEntity ->
                        marketService.buyItem(lisTokenDto, lisInfoEntity.getLisId()))
                .isPresent();
    }

    /**
     * Ищет сущность {@link LisInfoEntity} по имени предмета в базе данных.
     * Возвращает {@link Optional}, который может содержать найденную сущность или быть пустым, если предмет не найден.
     *
     * @param itemName имя предмета для поиска в базе данных
     * @return {@link Optional} с найденной сущностью или пустой {@link Optional}, если предмет не найден
     */
    public Optional<LisInfoEntity> getEntity(String itemName) {
        return lisInfoRepository.findByName(itemName);
    }

    /**
     * Получает все сущности {@link LisInfoEntity} из базы данных и возвращает их в виде объекта {@link PricesAnswerDto}.
     *
     * @return объект {@link PricesAnswerDto}, содержащий все сущности {@link LisInfoEntity}
     */
    @Override
    public PricesAnswerDto getEntities() {
        return new PricesAnswerDto(lisInfoRepository.findAll());
    }

    /**
     * Получает список ордеров на покупку с использованием переданного {@link ObjectMapper} для парсинга JSON-ответа.
     * Для каждого предмета в JSON проверяется, есть ли уже ордер на покупку, и если цена нового ордера ниже текущей,
     * то ордер заменяется. Возвращает карту, где ключ — это имя предмета, а значение — информация о предмете {@link LisInfoDto}.
     *
     * @param objectMapper объект для преобразования JSON в Java-объекты
     * @return карта с ордерами на покупку, где ключ — имя предмета, а значение — информация о предмете {@link LisInfoDto}
     */
    public Map<String, LisInfoDto> getBuyOrders(ObjectMapper objectMapper) {
        Map<String, LisInfoDto> buyOrders = new HashMap<>();

        String data = tmPriceParser.getJsonData();
        try {
            JsonNode rootNode = objectMapper.readTree(data);
            JsonNode itemsNode = rootNode
                    .path("items");
            if (!itemsNode.isEmpty()) {
                itemsNode.elements().forEachRemaining(element -> {
                    String itemName = element.get("name").asText();
                    Double price = element.get("price").asDouble();
                    Long lisId = element.get("id").asLong();
                    LisInfoDto lisInfoDto = new LisInfoDto(lisId, price);

                    Optional<LisInfoDto> optionalInfo = Optional.ofNullable(buyOrders.get(itemName));
                    optionalInfo.ifPresentOrElse(
                            info -> {
                                if (info.price() > price) {
                                    buyOrders.replace(itemName, lisInfoDto);
                                }
                            },
                            () -> buyOrders.put(itemName, lisInfoDto)
                    );
                });
            }
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }

        return buyOrders;
    }

    /**
     * Обновляет информацию о ценах предметов в базе данных.
     * Получает текущие ордера на покупку через {@link #getBuyOrders(ObjectMapper)}, а затем обновляет соответствующие записи в базе данных.
     */
    @Transactional
    public void setPriceMap() {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, LisInfoDto> usdBuyOrders = getBuyOrders(objectMapper);
        updateDbInfo(usdBuyOrders);
    }

    /**
     * Обновляет информацию о предметах в базе данных, используя переданную карту с информацией о предметах {@link LisInfoDto}.
     * Если предмет с таким именем уже существует в базе, обновляются его данные, иначе создается новая запись.
     *
     * @param lisInfoMap карта с информацией о предметах {@link LisInfoDto}, которые нужно сохранить в базе данных
     */
    @Override
    public void updateDbInfo(Map<String, LisInfoDto> lisInfoMap) {
        List<String> itemNames = new ArrayList<>(lisInfoMap.keySet());
        List<LisInfoEntity> existingEntities = lisInfoRepository.findAllByNameIn(itemNames);

        Map<String, LisInfoEntity> existingEntitiesMap = existingEntities.stream()
                .collect(Collectors.toMap(LisInfoEntity::getName, entity -> entity));

        List<LisInfoEntity> entitiesToSave = lisInfoMap.entrySet().stream()
                .map(entry -> {
                    String name = entry.getKey();
                    LisInfoDto dto = entry.getValue();

                    LisInfoEntity entity = existingEntitiesMap.getOrDefault(name, new LisInfoEntity());
                    entity.setName(name);
                    entity.setUsdPrice(dto.price());
                    entity.setLisId(dto.lisId());
                    return entity;
                })
                .toList();

        lisInfoRepository.saveAll(entitiesToSave);
    }
}
