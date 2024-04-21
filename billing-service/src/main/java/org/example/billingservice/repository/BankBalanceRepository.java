package org.example.billingservice.repository;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.state.HostInfo;
import org.example.billingservice.config.BillingConfiguration;
import org.example.billingservice.event.BankBalanceEvent;

import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class BankBalanceRepository extends KafkaStreamsRepository<String, BankBalanceEvent> {
    private final RestTemplate restTemplate;

    BankBalanceRepository(InteractiveQueryService queryService, HostInfo hostInfo, RestTemplate restTemplate) {
        super(queryService,
              new JsonSerde<>(),
              BillingConfiguration.BANK_BALANCE_STORE,
              hostInfo,
              "/billing-service/account/%s");
        this.restTemplate = restTemplate;
    }

    @Override
    protected Optional<BankBalanceEvent> findRemotely(String key, HostInfo hostInfo) {
        try {
            String url = "http://%s:%s" + findRemotelyUri.formatted(hostInfo.host(), hostInfo.port(), key);
            return Optional.ofNullable(restTemplate.getForEntity(url, BankBalanceEvent.class).getBody());
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }
}
