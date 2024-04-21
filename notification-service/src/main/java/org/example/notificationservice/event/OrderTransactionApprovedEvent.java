package org.example.notificationservice.event;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderTransactionApprovedEvent {
    private String account;
    private BigDecimal amount;
    private Integer orderId;
    private State state;
}
