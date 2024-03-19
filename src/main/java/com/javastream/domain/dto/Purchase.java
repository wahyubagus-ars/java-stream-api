package com.javastream.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase {
    @JsonProperty("purchase_id")
    private String purchaseId;

    @JsonProperty("product")
    private String product;

    @JsonProperty("count")
    private Integer count;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("category")
    private String category;
}
