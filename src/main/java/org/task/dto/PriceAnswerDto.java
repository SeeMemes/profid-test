package org.task.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PriceAnswerDto {
    private String itemName;
    private Double price;
}
