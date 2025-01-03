package org.task.services.parsers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.task.dto.LisInfoDto;
import org.task.services.MarketControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class TmPriceParser extends AbstractTmParser {
    private static final Logger log = LoggerFactory.getLogger(TmPriceParser.class);
    private final MarketControlService marketControlService;

    public TmPriceParser(
            @Autowired MarketControlService marketControlService
    ) {
        this.marketControlService = marketControlService;
    }

    public void setPriceMap() {
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, LisInfoDto> usdBuyOrders = getBuyOrdersForCurrency(objectMapper);

        marketControlService.updateDbInfo(usdBuyOrders);
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void updateStats() {
        log.info("TM parsing prices started");
        setPriceMap();
    }
}
