package org.example.billingservice.config;

import java.math.BigDecimal;
import java.util.function.Function;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueStore;
import org.example.billingservice.event.BankBalanceEvent;
import org.example.billingservice.event.BankTransactionEvent;
import org.example.billingservice.event.OrderEvent;
import org.example.billingservice.event.State;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaStreamBrancher;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.web.client.RestTemplate;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class BillingConfiguration {
    public static final String BANK_TRANSACTION = "bankTransaction";
    public static final String APPROVED_ORDER_TRANSACTION = "approvedOrderTransaction";
    public static final String REJECTED_ORDER_TRANSACTION = "rejectedOrderTransaction";
    public static final String BANK_BALANCE_STORE = "bankBalanceStore";

    @Bean
    public Function<KStream<String, OrderEvent>, KStream<String, BankTransactionEvent>> order() {
        return item -> item
                .map((key, value) -> new KeyValue<>(value.getAccount(), BankTransactionEvent.builder()
                        .orderId(value.getOrderId())
                        .account(value.getAccount())
                        .amount(value.getAmount())
                        .hash(value.getHash()).build()));
    }

    @Bean
    public Function<KStream<String, BankTransactionEvent>, KStream<String, BankTransactionEvent>> topology() {
        return input -> {
            Predicate<String, BankTransactionEvent> isSuccess = (k, v) -> State.APPROVED.equals(v.getState());
            Predicate<String, BankTransactionEvent> isFailed = (k, v) -> State.REJECTED.equals(v.getState());

            KStream<String, BankTransactionEvent> balance = input
                    .groupByKey()
                    .aggregate(BankBalanceEvent::new,
                               (key, value, aggregate) -> {
                                   BigDecimal v = aggregate.getBalance().add(value.getAmount());
                                   if (v.compareTo(BigDecimal.ZERO) >= 0) {
                                       aggregate.setBalance(v);
                                       aggregate.setLastTransactionState(State.APPROVED);
                                   } else {
                                       aggregate.setLastTransactionState(State.REJECTED);
                                   }

                                   aggregate.setAccount(value.getAccount());
                                   aggregate.setLastAmount(value.getAmount());
                                   aggregate.setLastOrderId(value.getOrderId());
                                   aggregate.setLastHash(value.getHash());

                                   return aggregate;
                               },
                               Materialized.<String, BankBalanceEvent, KeyValueStore<Bytes, byte[]>>as(BANK_BALANCE_STORE)
                                       .withKeySerde(Serdes.String())
                                       .withValueSerde(new JsonSerde<>(BankBalanceEvent.class)))
                    .mapValues(value -> BankTransactionEvent.builder()
                            .account(value.getAccount())
                            .amount(value.getLastAmount())
                            .hash(value.getLastHash())
                            .orderId(value.getLastOrderId())
                            .state(value.getLastTransactionState()).build())
                    .filter((key, value) -> value.getOrderId() != null)
                    .toStream();

            return new KafkaStreamBrancher<String, BankTransactionEvent>()
                    .branch(isSuccess,
                            ks -> ks.to(APPROVED_ORDER_TRANSACTION,
                                        Produced.with(Serdes.String(), new JsonSerde<>(BankTransactionEvent.class))))
                    .branch(isFailed,
                            ks -> ks.to(REJECTED_ORDER_TRANSACTION,
                                        Produced.with(Serdes.String(), new JsonSerde<>(BankTransactionEvent.class))))
                    .onTopOf(balance);
        };
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    @Bean
    public HostInfo hostInfo(@Value("${spring.cloud.stream.kafka.streams.binder.configuration.application.server}") String hostInfo) {
        String[] split = hostInfo.split(":");
        return new HostInfo(split[0], Integer.parseInt(split[1]));
    }
}
