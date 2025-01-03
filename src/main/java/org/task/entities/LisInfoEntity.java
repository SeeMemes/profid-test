package org.task.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class LisInfoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column
    private String name;
    @Column
    private Double usdPrice;
    @Column
    private Long lisId;

    public LisInfoEntity() {
        this.name = "";
        this.usdPrice = -1d;
    }

    public LisInfoEntity(String name, Long lisId) {
        this.name = name;
    }
}
