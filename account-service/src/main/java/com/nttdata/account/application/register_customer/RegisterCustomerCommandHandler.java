package com.nttdata.account.application.register_customer;

import com.nttdata.account.domain.Customer;
import reactor.core.publisher.Mono;

public interface RegisterCustomerCommandHandler {

    Mono<Customer> handle(RegisterCustomerCommand command);
}
