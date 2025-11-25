package com.nttdata.customer.client.application.get_customer_by_id;

import com.nttdata.customer.client.domain.Customer;
import reactor.core.publisher.Mono;

public interface GetCustomerByIdQueryHandler {

    Mono<Customer> handle(GetCustomerByIdQuery query);
}
