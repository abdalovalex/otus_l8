package org.example.orderservice.exception;

import org.example.orderservice.dto.ErrorDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = ResponseStatusException.class)
    protected ResponseEntity<ErrorDTO> handleConflict(ResponseStatusException exp) {
        ErrorDTO errorDto = new ErrorDTO();
        errorDto.setCode(0);
        errorDto.setMessage(exp.getReason());
        return new ResponseEntity<>(errorDto, exp.getStatusCode());
    }
}
