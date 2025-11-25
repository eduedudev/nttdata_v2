package com.nttdata.customer.client.infrastructure.rest;

import com.nttdata.customer.api.CustomersApi;
import com.nttdata.customer.api.model.CustomerRequest;
import com.nttdata.customer.api.model.CustomerResponse;
import com.nttdata.customer.client.application.mapper.CustomerMapper;
import com.nttdata.customer.client.application.usecase.CreateCustomerUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class CustomerController implements CustomersApi {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final CustomerMapper customerMapper;

    @Override
    public Mono<ResponseEntity<CustomerResponse>> createCustomer(Mono<CustomerRequest> customerRequest,
                                                                  ServerWebExchange exchange) {
        return customerRequest
                .map(customerMapper::toDomain)
                .flatMap(createCustomerUseCase::execute)
                .map(customerMapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCustomer(Long id, ServerWebExchange exchange) {
        return Mono.empty();
    }

    @Override
    public Mono<ResponseEntity<Flux<CustomerResponse>>> getAllCustomers(Integer page,
                                                                         Integer size,
                                                                         ServerWebExchange exchange) {
        return Mono.empty();
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> getCustomerById(Long id, ServerWebExchange exchange) {
        return Mono.empty();
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> getCustomerByIdentification(String identificacion,
                                                                               ServerWebExchange exchange) {
        return Mono.empty();
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> updateCustomer(Long id,
                                                                  Mono<CustomerRequest> customerRequest,
                                                                  ServerWebExchange exchange) {
        return Mono.empty();
    }
}
