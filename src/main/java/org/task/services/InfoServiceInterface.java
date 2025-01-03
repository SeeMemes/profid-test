package org.task.services;

import org.task.dto.LisInfoDto;
import org.task.dto.PricesAnswerDto;
import org.task.entities.LisInfoEntity;

import java.util.Map;
import java.util.Optional;

public interface InfoServiceInterface {
    Optional<LisInfoEntity> getEntity(String itemName);

    PricesAnswerDto getEntities();

    void updateDbInfo(Map<String, LisInfoDto> lisInfoMap);
}
