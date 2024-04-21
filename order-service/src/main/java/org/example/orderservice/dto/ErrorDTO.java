package org.example.orderservice.dto;

import lombok.Data;

@Data
public class ErrorDTO {
    private Integer code;
    private String message;
}
