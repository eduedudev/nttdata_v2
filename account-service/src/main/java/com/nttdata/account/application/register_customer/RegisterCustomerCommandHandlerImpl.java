package com.nttdata.account.application.register_customer;

import com.nttdata.account.domain.Customer;
import com.nttdata.account.domain.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterCustomerCommandHandlerImpl implements RegisterCustomerCommandHandler {

    private final CustomerRepository customerRepository;

    @Override
    public Mono<Customer> handle(RegisterCustomerCommand command) {
        return customerRepository.findById(command.getCustomerId())
                .flatMap(existingCustomer -> updateCustomer(existingCustomer, command))
                .switchIfEmpty(Mono.defer(() -> createCustomer(command)))
                .doOnSuccess(customer -> log.info("Customer registered/updated: {}", customer.getCustomerId()));
    }

    private Mono<Customer> updateCustomer(Customer existingCustomer, RegisterCustomerCommand command) {
        Customer updatedCustomer = Customer.builder()
                .customerId(existingCustomer.getCustomerId())
                .name(command.getName())
                .identification(command.getIdentification())
                .address(command.getAddress())
                .phone(command.getPhone())
                .status(command.getStatus())
                .build();
        return customerRepository.save(updatedCustomer);
    }

    private Mono<Customer> createCustomer(RegisterCustomerCommand command) {
        Customer newCustomer = Customer.builder()
                .customerId(command.getCustomerId())
                .name(command.getName())
                .identification(command.getIdentification())
                .address(command.getAddress())
                .phone(command.getPhone())
                .status(command.getStatus())
                .build();
        return customerRepository.save(newCustomer);
    }
}
