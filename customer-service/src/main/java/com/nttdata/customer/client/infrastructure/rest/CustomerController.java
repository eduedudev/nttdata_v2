package com.nttdata.customer.client.infrastructure.rest;

import com.nttdata.customer.api.CustomersApi;
import com.nttdata.customer.api.model.CustomerRequest;
import com.nttdata.customer.api.model.CustomerResponse;
import com.nttdata.customer.client.application.CustomerMapper;
import com.nttdata.customer.client.application.create_customer.CreateCustomerCommandHandler;
import com.nttdata.customer.client.application.delete_customer.DeleteCustomerCommand;
import com.nttdata.customer.client.application.delete_customer.DeleteCustomerCommandHandler;
import com.nttdata.customer.client.application.update_customer.UpdateCustomerCommandHandler;
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

    private final CreateCustomerCommandHandler createCustomerCommandHandler;
    private final UpdateCustomerCommandHandler updateCustomerCommandHandler;
    private final DeleteCustomerCommandHandler deleteCustomerCommandHandler;
    private final CustomerMapper customerMapper;

    @Override
    public Mono<ResponseEntity<CustomerResponse>> createCustomer(Mono<CustomerRequest> customerRequest,
                                                                  ServerWebExchange exchange) {
        return customerRequest
                .map(customerMapper::toCommand)
                .flatMap(createCustomerCommandHandler::handle)
                .map(customerMapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCustomer(Long id, ServerWebExchange exchange) {
        return deleteCustomerCommandHandler.handle(DeleteCustomerCommand.builder().customerId(id).build())
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
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
    public Mono<ResponseEntity<CustomerResponse>> getCustomerByIdentification(String identification,
                                                                               ServerWebExchange exchange) {
        return Mono.empty();
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> updateCustomer(Long id,
                                                                  Mono<CustomerRequest> customerRequest,
                                                                  ServerWebExchange exchange) {
        return customerRequest
                .map(request -> customerMapper.toUpdateCommand(id, request))
                .flatMap(updateCustomerCommandHandler::handle)
                .map(customerMapper::toResponse)
                .map(ResponseEntity::ok);
    }
}
