package org.example.billingservice.converter;

import org.example.billingservice.event.BankTransactionEvent;

import org.springframework.kafka.support.serializer.JsonSerde;

public class ConverterBankTransaction extends JsonSerde<BankTransactionEvent> {
}
