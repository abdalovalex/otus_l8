package org.example.billingservice.event;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BankBalanceEvent {
    private String account;
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;
    private BigDecimal lastAmount;
    private String lastHash;
    private Integer lastOrderId;
    private State lastTransactionState;
}
