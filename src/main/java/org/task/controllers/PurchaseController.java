package org.task.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.task.dto.LisTokenDto;
import org.task.dto.PricesAnswerDto;
import org.task.dto.PurchaseInfoDto;
import org.task.entities.LisInfoEntity;
import org.task.services.MarketControlService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@RestController
public class PurchaseController {
    private final MarketControlService tmMarketControlService;

    public PurchaseController(
            @Autowired MarketControlService tmMarketControlService
    ) {
        this.tmMarketControlService = tmMarketControlService;
    }

    @GetMapping("/price/{itemName}")
    public ResponseEntity<?> getPrice(
            @PathVariable String itemName
    ) {
        Optional<LisInfoEntity> optionalTmInfo = tmMarketControlService.getEntity(itemName);
        if (optionalTmInfo.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(optionalTmInfo.get());
    }

    @GetMapping("/prices")
    public ResponseEntity<?> getPrices() {
        PricesAnswerDto pricesList = tmMarketControlService.getEntities();
        return ResponseEntity.ok(pricesList);
    }

    @PostMapping("/purchase")
    public ResponseEntity<?> purchase(
            @RequestBody PurchaseInfoDto purchaseInfoDto
    ) {
        LisTokenDto lisTokenDto = new LisTokenDto(
                purchaseInfoDto.apiKey(),
                purchaseInfoDto.steamPartner(),
                purchaseInfoDto.steamToken()
        );
        String itemName = purchaseInfoDto.itemName();
        boolean buyItem = tmMarketControlService.buyItem(lisTokenDto, itemName);
        if (!buyItem)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(buyItem);
    }
}
