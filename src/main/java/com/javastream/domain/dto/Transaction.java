package com.javastream.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("user")
    private String user;

    @JsonProperty("transaction_date")
    private Long transactionDate;

    @JsonProperty("shipping")
    private String shipping;

    @JsonProperty("purchases")
    private List<Purchase> purchases;
}
