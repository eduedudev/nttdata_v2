package com.nttdata.customer.client.infrastructure.rest;

import com.nttdata.customer.api.CustomersApi;
import com.nttdata.customer.api.model.CustomerRequest;
import com.nttdata.customer.api.model.CustomerResponse;
import com.nttdata.customer.client.application.CustomerMapper;
import com.nttdata.customer.client.application.create_customer.CreateCustomerCommandHandler;
import com.nttdata.customer.client.application.delete_customer.DeleteCustomerCommand;
import com.nttdata.customer.client.application.delete_customer.DeleteCustomerCommandHandler;
import com.nttdata.customer.client.application.get_all_customers.GetAllCustomersQuery;
import com.nttdata.customer.client.application.get_all_customers.GetAllCustomersQueryHandler;
import com.nttdata.customer.client.application.get_customer_by_id.GetCustomerByIdQuery;
import com.nttdata.customer.client.application.get_customer_by_id.GetCustomerByIdQueryHandler;
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
    private final GetAllCustomersQueryHandler getAllCustomersQueryHandler;
    private final GetCustomerByIdQueryHandler getCustomerByIdQueryHandler;
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
        GetAllCustomersQuery query = GetAllCustomersQuery.builder()
                .page(page)
                .size(size)
                .build();
        Flux<CustomerResponse> customers = getAllCustomersQueryHandler.handle(query)
                .map(customerMapper::toResponse);
        return Mono.just(ResponseEntity.ok(customers));
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> getCustomerById(Long id, ServerWebExchange exchange) {
        return getCustomerByIdQueryHandler.handle(GetCustomerByIdQuery.builder().customerId(id).build())
                .map(customerMapper::toResponse)
                .map(ResponseEntity::ok);
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
