package org.example.billingservice.converter;

import org.example.billingservice.event.OrderEvent;

import org.springframework.kafka.support.serializer.JsonSerde;

public class ConverterOrder extends JsonSerde<OrderEvent> {
}
