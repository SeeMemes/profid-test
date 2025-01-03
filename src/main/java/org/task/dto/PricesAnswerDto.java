package org.task.dto;

import org.task.entities.LisInfoEntity;

import java.util.List;

public record PricesAnswerDto(List<LisInfoEntity> infoEntities) {
}
