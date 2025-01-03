import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.task.dto.LisTokenDto;
import org.task.services.MarketService;
import org.task.util.HttpClientService;

import java.io.IOException;

class PurchaseTest {
    @Mock
    private CloseableHttpClient httpClient;
    @Mock
    private HttpClientService clientService;
    private MarketService marketService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(clientService.getHttpClient()).thenReturn(httpClient);
        marketService = new MarketService(clientService);
    }

    /**
     * Тестирует метод {@link MarketService#buyItem} на успешную отправку HTTP-запроса.
     * Проверяется, что выполняется правильный HTTP-запрос с нужными заголовками.
     */
    @Test
    void testBuyItem_sendsHttpRequest() throws Exception {
        LisTokenDto lisTokenDto = new LisTokenDto("apiKey", "steamPartner", "steamToken");
        Long itemId = 123L;

        CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
        when(mockResponse.getCode()).thenReturn(200);
        when(httpClient.execute(any(HttpPost.class))).thenReturn(mockResponse);

        boolean result = marketService.buyItem(lisTokenDto, itemId);

        ArgumentCaptor<HttpPost> captor = ArgumentCaptor.forClass(HttpPost.class);
        verify(httpClient, times(1)).execute(captor.capture());

        HttpPost executedRequest = captor.getValue();

        assertEquals("https://api.lis-skins.com/v1/market/buy", executedRequest.getUri().toString());
        assertEquals("Bearer apiKey", executedRequest.getFirstHeader("Authorization").getValue());
        assertEquals("application/json", executedRequest.getFirstHeader("Content-Type").getValue());

        assertTrue(result);
    }

    /**
     * Тестирует метод {@link MarketService#buyItem} на обработку исключения {@link IOException}.
     * Проверяется, что в случае возникновения IOException результат метода будет false.
     */
    @Test
    void testBuyItem_handlesIOException() throws Exception {
        LisTokenDto lisTokenDto = new LisTokenDto("apiKey", "steamPartner", "steamToken");
        Long itemId = 123L;

        when(httpClient.execute(any(HttpPost.class))).thenThrow(new IOException("Test exception"));
        boolean result = marketService.buyItem(lisTokenDto, itemId);

        assertFalse(result);
    }
}