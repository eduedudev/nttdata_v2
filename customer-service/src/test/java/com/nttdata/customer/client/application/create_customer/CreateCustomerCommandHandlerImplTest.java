package com.nttdata.customer.client.application.create_customer;

import com.nttdata.customer.client.domain.Customer;
import com.nttdata.customer.client.domain.CustomerAlreadyExistsException;
import com.nttdata.customer.client.domain.CustomerCreatedEvent;
import com.nttdata.customer.client.domain.CustomerMother;
import com.nttdata.customer.client.domain.CustomerRepository;
import com.nttdata.customer.client.domain.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCustomerCommandHandlerImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private CreateCustomerCommandHandlerImpl createCustomerCommandHandler;

    private CreateCustomerCommand command;
    private Customer savedCustomer;

    @BeforeEach
    void setUp() {
        command = CreateCustomerCommandMother.createDefault();
        savedCustomer = CustomerMother.createDefault();
    }

    @Test
    void shouldCreateCustomerSuccessfully() {
        when(customerRepository.existsByIdentification(anyString())).thenReturn(Mono.just(false));
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(savedCustomer));
        when(domainEventPublisher.publish(any(CustomerCreatedEvent.class))).thenReturn(Mono.empty());

        StepVerifier.create(createCustomerCommandHandler.handle(command))
                .expectNextMatches(result ->
                        result.getCustomerId().equals(1L) &&
                        result.getName().equals("John Doe") &&
                        result.getIdentification().equals("1234567890"))
                .verifyComplete();

        verify(customerRepository).existsByIdentification("1234567890");
        verify(customerRepository).save(any(Customer.class));
        verify(domainEventPublisher).publish(any(CustomerCreatedEvent.class));
    }

    @Test
    void shouldThrowExceptionWhenCustomerAlreadyExists() {
        when(customerRepository.existsByIdentification(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(createCustomerCommandHandler.handle(command))
                .expectError(CustomerAlreadyExistsException.class)
                .verify();

        verify(customerRepository).existsByIdentification("1234567890");
        verify(customerRepository, never()).save(any(Customer.class));
        verify(domainEventPublisher, never()).publish(any(CustomerCreatedEvent.class));
    }

    @Test
    void shouldPublishCustomerCreatedEvent() {
        when(customerRepository.existsByIdentification(anyString())).thenReturn(Mono.just(false));
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(savedCustomer));
        when(domainEventPublisher.publish(any(CustomerCreatedEvent.class))).thenReturn(Mono.empty());

        StepVerifier.create(createCustomerCommandHandler.handle(command))
                .expectNextCount(1)
                .verifyComplete();

        verify(domainEventPublisher).publish(argThat(event -> {
            CustomerCreatedEvent customerEvent = (CustomerCreatedEvent) event;
            return customerEvent.getEventType().equals("CustomerCreated") &&
                    customerEvent.getAggregateId().equals("1") &&
                    customerEvent.getName().equals("John Doe");
        }));
    }

    @Test
    void shouldSetCreatedAtAndUpdatedAtWhenCreatingCustomer() {
        when(customerRepository.existsByIdentification(anyString())).thenReturn(Mono.just(false));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            return Mono.just(Customer.builder()
                    .customerId(1L)
                    .name(c.getName())
                    .gender(c.getGender())
                    .identification(c.getIdentification())
                    .address(c.getAddress())
                    .phone(c.getPhone())
                    .password(c.getPassword())
                    .status(c.getStatus())
                    .createdAt(c.getCreatedAt())
                    .updatedAt(c.getUpdatedAt())
                    .build());
        });
        when(domainEventPublisher.publish(any(CustomerCreatedEvent.class))).thenReturn(Mono.empty());

        StepVerifier.create(createCustomerCommandHandler.handle(command))
                .expectNextMatches(result ->
                        result.getCreatedAt() != null &&
                        result.getUpdatedAt() != null)
                .verifyComplete();
    }

    @Test
    void shouldCreateCustomerWithDifferentIdentification() {
        CreateCustomerCommand customCommand = CreateCustomerCommandMother.createWithIdentification("9999999999");
        Customer savedCustom = CustomerMother.createWithIdentification("9999999999");

        when(customerRepository.existsByIdentification("9999999999")).thenReturn(Mono.just(false));
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(savedCustom));
        when(domainEventPublisher.publish(any(CustomerCreatedEvent.class))).thenReturn(Mono.empty());

        StepVerifier.create(createCustomerCommandHandler.handle(customCommand))
                .expectNextMatches(result ->
                        result.getIdentification().equals("9999999999"))
                .verifyComplete();

        verify(customerRepository).existsByIdentification("9999999999");
    }

    @Test
    void shouldCreateFemaleCustomer() {
        CreateCustomerCommand femaleCommand = CreateCustomerCommandMother.createFemale();
        Customer femaleCustomer = CustomerMother.createFemale();

        when(customerRepository.existsByIdentification("0987654321")).thenReturn(Mono.just(false));
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(femaleCustomer));
        when(domainEventPublisher.publish(any(CustomerCreatedEvent.class))).thenReturn(Mono.empty());

        StepVerifier.create(createCustomerCommandHandler.handle(femaleCommand))
                .expectNextMatches(result ->
                        result.getName().equals("Jane Doe") &&
                        result.getGender().name().equals("FEMALE"))
                .verifyComplete();
    }

    @Test
    void shouldCreateInactiveCustomer() {
        CreateCustomerCommand inactiveCommand = CreateCustomerCommandMother.createInactive();
        Customer inactiveCustomer = CustomerMother.createInactive();

        when(customerRepository.existsByIdentification(anyString())).thenReturn(Mono.just(false));
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(inactiveCustomer));
        when(domainEventPublisher.publish(any(CustomerCreatedEvent.class))).thenReturn(Mono.empty());

        StepVerifier.create(createCustomerCommandHandler.handle(inactiveCommand))
                .expectNextMatches(result -> !result.getStatus())
                .verifyComplete();
    }
}
