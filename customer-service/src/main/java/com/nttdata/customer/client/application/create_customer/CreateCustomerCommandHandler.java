package com.nttdata.customer.client.application.create_customer;

import com.nttdata.customer.client.domain.Customer;
import reactor.core.publisher.Mono;

public interface CreateCustomerCommandHandler {

    Mono<Customer> handle(CreateCustomerCommand command);
}
