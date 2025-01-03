package org.task.services.parsers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.net.URIBuilder;
import org.task.dto.LisInfoDto;
import org.task.services.MarketControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class TmPriceParser{
    private static final Logger log = LoggerFactory.getLogger(TmPriceParser.class);

    /**
     * Получает данные в формате JSON с внешнего API, выполняя HTTP GET запрос.
     * Если запрос успешен (статус 200), возвращает строку с JSON данными.
     * В случае ошибки (например, проблемы с подключением или некорректный URL) возвращает пустую строку.
     *
     * @return строку с JSON данными, если запрос выполнен успешно, или пустую строку в случае ошибки
     */
    public String getJsonData() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            URI tmURI = new URIBuilder("https://lis-skins.com/market_export_json/api_csgo_full.json")
                    .build();
            HttpGet httpGet = new HttpGet(tmURI);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getCode();

                if (statusCode == 200) {
                    return convertInputStreamToString(response.getEntity().getContent());
                }
            }
        } catch (IOException ignored) {
            log.debug("Couldn't parse items from TM. Trying again...");
        } catch (URISyntaxException e) {
            log.error("Wrong URL for Tm: " + e.getMessage());
        }

        return "";
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder result = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            result.append(line);
        }

        return result.toString();
    }
}
