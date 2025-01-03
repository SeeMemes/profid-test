package org.task.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.task.dto.LisTokenDto;
import org.task.dto.PurchaseRequestDto;
import org.task.util.HttpClientService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Service
public class MarketService {
    private static final Logger log = LoggerFactory.getLogger(MarketService.class);
    private final CloseableHttpClient httpClient;
    private final Gson gson;

    public MarketService(
            @Autowired HttpClientService clientService
    ) {
        httpClient = clientService.getHttpClient();
        gson = new Gson();
    }

    public boolean buyItem(LisTokenDto lisTokenDto, Long itemId) {
        try {
            URI requestURI = new URI("https://api.lis-skins.com/v1/market/buy");
            HttpPost httpPost = new HttpPost(requestURI);
            httpPost.setHeader("Authorization", "Bearer " + lisTokenDto.apiKey());
            httpPost.setHeader("Content-Type", "application/json");

            PurchaseRequestDto purchaseRequest = new PurchaseRequestDto(
                    itemId,
                    lisTokenDto.steamPartner(),
                    lisTokenDto.steamToken()
            );

            httpPost.setEntity(new StringEntity(gson.toJson(purchaseRequest)));

            httpClient.execute(httpPost);
            return true;
        } catch (IOException e) {
            log.error("Could not connect: " + e.getMessage());
        } catch (URISyntaxException e) {
            log.error("Could not set price on TM by the given URI: " + e.getMessage());
        }
        return false;
    }
}