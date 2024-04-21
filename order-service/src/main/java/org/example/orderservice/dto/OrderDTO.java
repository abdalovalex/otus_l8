package org.example.orderservice.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDTO {
    private Integer orderId;
    private String account;
    private BigDecimal amount;
    private String hash;
}
