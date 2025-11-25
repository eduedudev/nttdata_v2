package com.nttdata.customer.client.application.usecase.impl;

import com.nttdata.customer.client.application.usecase.CreateCustomerUseCase;
import com.nttdata.customer.client.domain.exception.CustomerAlreadyExistsException;
import com.nttdata.customer.client.domain.model.Customer;
import com.nttdata.customer.client.domain.port.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class CreateCustomerUseCaseImpl implements CreateCustomerUseCase {

    private final CustomerRepository customerRepository;

    @Override
    public Mono<Customer> execute(Customer customer) {
        return customerRepository.existsByIdentification(customer.getIdentification())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new CustomerAlreadyExistsException(customer.getIdentification()));
                    }
                    customer.setCreatedAt(OffsetDateTime.now());
                    customer.setUpdatedAt(OffsetDateTime.now());
                    return customerRepository.save(customer);
                });
    }
}
