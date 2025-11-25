package com.nttdata.customer.client.application.delete_customer;

import reactor.core.publisher.Mono;

public interface DeleteCustomerCommandHandler {

    Mono<Void> handle(DeleteCustomerCommand command);
}
