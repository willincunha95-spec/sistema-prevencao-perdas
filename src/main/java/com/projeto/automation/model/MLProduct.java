package com.projeto.automation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MLProduct {
    private String id;
    private String title;
    private BigDecimal price;
    private String permalink;
    private String thumbnail;
    private String seller;
    private String condition;
    private String shipping;
}
