package com.nttdata.account.domain;

import reactor.core.publisher.Mono;

/**
 * Port for customer persistence operations.
 */
public interface CustomerRepository {

    Mono<Customer> save(Customer customer);

    Mono<Customer> findById(Long customerId);

    Mono<Customer> findByIdentification(String identification);

    Mono<Void> deleteById(Long customerId);

    Mono<Boolean> existsById(Long customerId);
}
