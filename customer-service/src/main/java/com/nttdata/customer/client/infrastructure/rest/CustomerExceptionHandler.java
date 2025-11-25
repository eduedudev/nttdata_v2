package com.nttdata.customer.client.infrastructure.rest;

import com.nttdata.customer.api.model.ErrorResponse;
import com.nttdata.customer.client.domain.CustomerAlreadyExistsException;
import com.nttdata.customer.client.domain.CustomerNotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@RestControllerAdvice
@Order(1)
public class CustomerExceptionHandler {

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleCustomerAlreadyExists(CustomerAlreadyExistsException ex,
                                                                            ServerWebExchange exchange) {
        ErrorResponse error = buildErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(error));
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleCustomerNotFound(CustomerNotFoundException ex,
                                                                       ServerWebExchange exchange) {
        ErrorResponse error = buildErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }

    private ErrorResponse buildErrorResponse(Integer status, String error, String message, String path) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(OffsetDateTime.now());
        errorResponse.setStatus(status);
        errorResponse.setError(error);
        errorResponse.setMessage(message);
        errorResponse.setPath(path);
        return errorResponse;
    }
}
