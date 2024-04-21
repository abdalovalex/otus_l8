package org.example.billingservice.dto;

import lombok.Data;

@Data
public class ErrorDTO {
    private Integer code;
    private String message;
}
