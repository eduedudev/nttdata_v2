package com.nttdata.customer.client.domain.port;

import com.nttdata.customer.client.domain.model.Customer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerRepository {

    Mono<Customer> save(Customer customer);

    Mono<Customer> findById(Long id);

    Mono<Customer> findByIdentification(String identification);

    Flux<Customer> findAll();

    Mono<Void> deleteById(Long id);

    Mono<Boolean> existsByIdentification(String identification);
}
