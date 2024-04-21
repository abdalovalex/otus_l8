package org.example.billingservice.event;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankTransactionEvent {
    private String account;
    private BigDecimal amount;
    private Integer orderId;
    private String hash;

    @Builder.Default
    private State state = State.CREATED;
}
