package org.example.orderservice.event;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class OrderEvent {
    private Integer orderId;
    private String account;
    private BigDecimal amount;
    private String hash;
}
