package com.nttdata.customer.client.application.update_customer;

import com.nttdata.customer.client.domain.Customer;
import com.nttdata.customer.client.domain.CustomerNotFoundException;
import com.nttdata.customer.client.domain.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class UpdateCustomerCommandHandlerImpl implements UpdateCustomerCommandHandler {

    private final CustomerRepository customerRepository;

    @Override
    public Mono<Customer> handle(UpdateCustomerCommand command) {
        return customerRepository.findById(command.getCustomerId())
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(command.getCustomerId())))
                .flatMap(existingCustomer -> {
                    Customer updatedCustomer = Customer.builder()
                            .customerId(existingCustomer.getCustomerId())
                            .name(command.getName())
                            .gender(command.getGender())
                            .identification(existingCustomer.getIdentification())
                            .address(command.getAddress())
                            .phone(command.getPhone())
                            .password(command.getPassword())
                            .status(command.getStatus())
                            .createdAt(existingCustomer.getCreatedAt())
                            .updatedAt(OffsetDateTime.now())
                            .build();
                    return customerRepository.save(updatedCustomer);
                });
    }
}
