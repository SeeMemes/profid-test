package org.task.services.parsers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.task.dto.LisInfoDto;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractTmParser {
    private static final Logger log = LoggerFactory.getLogger(AbstractTmParser.class);

    protected AbstractTmParser() {
    }

    protected Map<String, LisInfoDto> getBuyOrdersForCurrency(ObjectMapper objectMapper) {
        Map<String, LisInfoDto> buyOrders = new HashMap<>();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            URI tmURI = new URIBuilder("https://lis-skins.com/market_export_json/api_csgo_full.json")
                    .build();
            HttpGet httpGet = new HttpGet(tmURI);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getCode();

                if (statusCode == 200) {
                    JsonNode rootNode = objectMapper.readTree(response.getEntity().getContent());
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
                } else {
                    log.error("Got unsupported status: " + statusCode);
                }
            }
        } catch (IOException ignored) {
            log.debug("Couldn't parse items from TM. Trying again...");
        } catch (URISyntaxException e) {
            log.error("Wrong URL for Tm: " + e.getMessage());
        }

        return buyOrders;
    }
}
