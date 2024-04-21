package org.example.billingservice.repository;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.example.billingservice.exception.AccountException;
import org.example.billingservice.exception.KafkaException;

import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;

@Slf4j
@AllArgsConstructor
public abstract class KafkaStreamsRepository<K, V> {
    private final InteractiveQueryService queryService;
    private final Serde<K> keySerde;
    private final String storeName;

    protected final HostInfo hostInfo;
    protected String findRemotelyUri;

    public Optional<V> find(K key) throws KafkaException {
        HostInfo activeHost = queryService.getHostInfo(storeName,
                                                       key,
                                                       keySerde.serializer());
        if ("unavailable".equals(activeHost.host())) {
            throw new KafkaException("Хост не доступен");
        }
        if (hostInfo.equals(activeHost)) {
            return findLocally(key);
        }
        return findRemotely(key, activeHost);
    }

    private Optional<V> findLocally(K key) {
        return Optional.ofNullable(getStore().get(key));
    }

    private ReadOnlyKeyValueStore<K, V> getStore() {
        return queryService.getQueryableStore(storeName, QueryableStoreTypes.keyValueStore());
    }


    protected abstract Optional<V> findRemotely(K key, HostInfo hostInfo);
}
