package org.example.billingservice.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class IncomeMoneyDTO {
    private String account;
    private BigDecimal amount;
    private String hash;
}
