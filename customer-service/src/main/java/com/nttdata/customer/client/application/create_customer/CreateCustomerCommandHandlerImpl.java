package com.nttdata.customer.client.application.create_customer;

import com.nttdata.customer.client.domain.Customer;
import com.nttdata.customer.client.domain.CustomerAlreadyExistsException;
import com.nttdata.customer.client.domain.CustomerCreatedEvent;
import com.nttdata.customer.client.domain.CustomerRepository;
import com.nttdata.customer.client.domain.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class CreateCustomerCommandHandlerImpl implements CreateCustomerCommandHandler {

    private final CustomerRepository customerRepository;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    public Mono<Customer> handle(CreateCustomerCommand command) {
        return customerRepository.existsByIdentification(command.getIdentification())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new CustomerAlreadyExistsException(command.getIdentification()));
                    }
                    return customerRepository.save(buildCustomer(command));
                })
                .flatMap(this::publishCustomerCreatedEvent);
    }

    private Mono<Customer> publishCustomerCreatedEvent(Customer customer) {
        CustomerCreatedEvent event = CustomerCreatedEvent.fromCustomer(customer);
        return domainEventPublisher.publish(event)
                .thenReturn(customer);
    }

    private Customer buildCustomer(CreateCustomerCommand command) {
        OffsetDateTime now = OffsetDateTime.now();
        return Customer.builder()
                .name(command.getName())
                .gender(command.getGender())
                .identification(command.getIdentification())
                .address(command.getAddress())
                .phone(command.getPhone())
                .password(command.getPassword())
                .status(command.getStatus())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
