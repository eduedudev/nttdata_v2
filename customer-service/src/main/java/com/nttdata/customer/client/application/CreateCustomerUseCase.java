package com.nttdata.customer.client.application.usecase;

import com.nttdata.customer.client.domain.model.Customer;
import reactor.core.publisher.Mono;

public interface CreateCustomerUseCase {

    Mono<Customer> execute(Customer customer);
}
