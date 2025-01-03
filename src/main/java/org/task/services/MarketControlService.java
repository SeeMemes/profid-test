package org.task.services;

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

    public MarketControlService(
            @Autowired HttpClientService httpClientService,
            @Autowired LisInfoRepository lisInfoRepository,
            @Autowired MarketService marketService
    ) {
        this.httpClient = httpClientService.getHttpClient();
        this.lisInfoRepository = lisInfoRepository;
        this.marketService = marketService;
    }

    public boolean buyItem(LisTokenDto lisTokenDto, String itemName) {
        Optional<LisInfoEntity> optionalItem = lisInfoRepository.findByName(itemName);
        return optionalItem
                .filter(lisInfoEntity ->
                        marketService.buyItem(lisTokenDto, lisInfoEntity.getLisId()))
                .isPresent();
    }

    public Optional<LisInfoEntity> getEntity(String itemName) {
        return lisInfoRepository.findByName(itemName);
    }

    @Override
    public PricesAnswerDto getEntities() {
        return new PricesAnswerDto(lisInfoRepository.findAll());
    }

    @Override
    @Transactional
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
