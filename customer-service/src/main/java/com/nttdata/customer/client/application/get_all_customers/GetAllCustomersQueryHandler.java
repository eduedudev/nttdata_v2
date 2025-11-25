package com.nttdata.customer.client.application.get_all_customers;

import com.nttdata.customer.client.domain.Customer;
import reactor.core.publisher.Flux;

public interface GetAllCustomersQueryHandler {

    Flux<Customer> handle(GetAllCustomersQuery query);
}
