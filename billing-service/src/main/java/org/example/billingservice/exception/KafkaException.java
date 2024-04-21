package org.example.billingservice.exception;

public class KafkaException extends Exception {
    public KafkaException(String message) {
        super(message);
    }
}
