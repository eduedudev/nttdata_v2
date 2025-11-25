package com.nttdata.account.infrastructure.rest;

import com.nttdata.account.api.model.ErrorResponse;
import com.nttdata.account.domain.AccountAlreadyExistsException;
import com.nttdata.account.domain.AccountNotFoundException;
import com.nttdata.account.domain.CustomerNotFoundException;
import com.nttdata.account.domain.InsufficientBalanceException;
import com.nttdata.account.domain.InvalidAmountException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAccountNotFound(AccountNotFoundException ex) {
        log.warn("Account not found: {}", ex.getMessage());
        return Mono.just(buildErrorResponse(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(AccountAlreadyExistsException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAccountAlreadyExists(AccountAlreadyExistsException ex) {
        log.warn("Account already exists: {}", ex.getMessage());
        return Mono.just(buildErrorResponse(HttpStatus.CONFLICT, "ACCOUNT_ALREADY_EXISTS", ex.getMessage()));
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleCustomerNotFound(CustomerNotFoundException ex) {
        log.warn("Customer not found: {}", ex.getMessage());
        return Mono.just(buildErrorResponse(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInsufficientBalance(InsufficientBalanceException ex) {
        log.warn("Insufficient balance: {}", ex.getMessage());
        return Mono.just(buildErrorResponse(HttpStatus.BAD_REQUEST, "INSUFFICIENT_BALANCE", ex.getMessage()));
    }

    @ExceptionHandler(InvalidAmountException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidAmount(InvalidAmountException ex) {
        log.warn("Invalid amount: {}", ex.getMessage());
        return Mono.just(buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", ex.getMessage()));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationException(WebExchangeBindException ex) {
        String details = ex.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation error: {}", details);
        return Mono.just(buildErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", details));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return Mono.just(buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return Mono.just(buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", 
                "An unexpected error occurred"));
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String code, String message) {
        ErrorResponse error = new ErrorResponse();
        error.setStatus(status.value());
        error.setError(code);
        error.setMessage(message);
        error.setTimestamp(OffsetDateTime.now());
        return ResponseEntity.status(status).body(error);
    }
}
