package com.nttdata.customer.client.application.update_customer;

import com.nttdata.customer.client.domain.Customer;
import reactor.core.publisher.Mono;

public interface UpdateCustomerCommandHandler {

    Mono<Customer> handle(UpdateCustomerCommand command);
}
