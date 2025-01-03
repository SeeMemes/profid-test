package org.task.dto;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class PurchaseRequestDto {
    private List<String> ids;
    private String partner;
    private String token;

    public PurchaseRequestDto(long ids, String partner, String token) {
        this.ids = Collections.singletonList(Long.toString(ids));
        this.partner = partner;
        this.token = token;
    }
}
