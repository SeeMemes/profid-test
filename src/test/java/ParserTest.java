import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.task.dto.LisInfoDto;
import org.task.repositories.LisInfoRepository;
import org.task.services.MarketControlService;
import org.task.services.MarketService;
import org.task.services.parsers.TmPriceParser;
import org.task.util.HttpClientService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ParserTest {
    @Mock
    HttpClientService httpClientService;
    @Mock
    LisInfoRepository lisInfoRepository;
    @Mock
    MarketService marketService;
    @Mock
    private TmPriceParser tmPriceParser;
    private MarketControlService marketControlService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.marketControlService = new MarketControlService(
                httpClientService,
                lisInfoRepository,
                marketService,
                tmPriceParser
        );
    }

    /**
     * Тестирует метод {@link MarketControlService#getBuyOrders} на успешную обработку корректного JSON-ответа.
     * Проверяется, что правильные данные преобразуются в список объектов {@link LisInfoDto}.
     */
    @Test
    void testGetBuyOrders_successResponse() throws Exception {
        String jsonResponse = """
            {
                "items": [
                    { "name": "Item1", "price": 100.0, "id": 1 },
                    { "name": "Item2", "price": 200.0, "id": 2 }
                ]
            }
        """;
        when(tmPriceParser.getJsonData()).thenReturn(jsonResponse);

        Map<String, LisInfoDto> result = marketControlService.getBuyOrders(objectMapper);

        assertEquals(2, result.size());
        assertEquals(new LisInfoDto(1L, 100.0), result.get("Item1"));
        assertEquals(new LisInfoDto(2L, 200.0), result.get("Item2"));
        verify(tmPriceParser, times(1)).getJsonData();
    }

    /**
     * Тестирует метод {@link MarketControlService#getBuyOrders} на обработку некорректного JSON-ответа.
     * Проверяется, что в случае ошибки парсинга возвращается пустой результат.
     */
    @Test
    void testGetBuyOrders_invalidJson() {
        String invalidJson = "Invalid JSON";
        when(tmPriceParser.getJsonData()).thenReturn(invalidJson);

        Map<String, LisInfoDto> result = marketControlService.getBuyOrders(objectMapper);

        assertTrue(result.isEmpty());
    }
}
