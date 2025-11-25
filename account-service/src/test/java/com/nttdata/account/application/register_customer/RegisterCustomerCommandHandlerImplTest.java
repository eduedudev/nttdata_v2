package com.nttdata.account.application.register_customer;

import com.nttdata.account.domain.Customer;
import com.nttdata.account.domain.CustomerMother;
import com.nttdata.account.domain.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterCustomerCommandHandlerImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private RegisterCustomerCommandHandlerImpl registerCustomerCommandHandler;

    private RegisterCustomerCommand command;
    private Customer customer;

    @BeforeEach
    void setUp() {
        command = RegisterCustomerCommandMother.createDefault();
        customer = CustomerMother.createDefault();
    }

    @Test
    void shouldCreateNewCustomerWhenNotExists() {
        when(customerRepository.findById(anyLong())).thenReturn(Mono.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(customer));

        StepVerifier.create(registerCustomerCommandHandler.handle(command))
                .expectNextMatches(result ->
                        result.getCustomerId().equals(1L) &&
                        result.getName().equals("John Doe") &&
                        result.getIdentification().equals("1234567890"))
                .verifyComplete();

        verify(customerRepository).findById(1L);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void shouldUpdateCustomerWhenAlreadyExists() {
        Customer existingCustomer = CustomerMother.createDefault();
        RegisterCustomerCommand updateCommand = RegisterCustomerCommandMother.createWithName("Jane Updated");

        when(customerRepository.findById(anyLong())).thenReturn(Mono.just(existingCustomer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            return Mono.just(c);
        });

        StepVerifier.create(registerCustomerCommandHandler.handle(updateCommand))
                .expectNextMatches(result ->
                        result.getName().equals("Jane Updated"))
                .verifyComplete();

        verify(customerRepository).findById(1L);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void shouldPreserveCustomerIdOnUpdate() {
        Customer existingCustomer = CustomerMother.createWithId(99L);
        RegisterCustomerCommand updateCommand = RegisterCustomerCommandMother.createWithId(99L);

        when(customerRepository.findById(99L)).thenReturn(Mono.just(existingCustomer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(registerCustomerCommandHandler.handle(updateCommand))
                .expectNextMatches(result -> result.getCustomerId().equals(99L))
                .verifyComplete();
    }

    @Test
    void shouldCreateInactiveCustomer() {
        RegisterCustomerCommand inactiveCommand = RegisterCustomerCommandMother.createInactive();
        Customer inactiveCustomer = CustomerMother.createInactive();

        when(customerRepository.findById(anyLong())).thenReturn(Mono.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(inactiveCustomer));

        StepVerifier.create(registerCustomerCommandHandler.handle(inactiveCommand))
                .expectNextMatches(result -> Boolean.FALSE.equals(result.getStatus()))
                .verifyComplete();
    }

    @Test
    void shouldSaveCustomerWithAllFields() {
        when(customerRepository.findById(anyLong())).thenReturn(Mono.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer saved = inv.getArgument(0);
            return Mono.just(saved);
        });

        StepVerifier.create(registerCustomerCommandHandler.handle(command))
                .expectNextMatches(result ->
                        result.getCustomerId().equals(1L) &&
                        result.getName().equals("John Doe") &&
                        result.getIdentification().equals("1234567890") &&
                        result.getAddress().equals("123 Main Street") &&
                        result.getPhone().equals("+573001234567") &&
                        result.getStatus().equals(true))
                .verifyComplete();
    }

    @Test
    void shouldHandleCustomerFromKafkaEvent() {
        // Simulating data that would come from CustomerCreatedEvent
        RegisterCustomerCommand kafkaCommand = RegisterCustomerCommand.builder()
                .customerId(123L)
                .name("Kafka Customer")
                .identification("9876543210")
                .address("456 Kafka Street")
                .phone("+573009876543")
                .status(true)
                .build();

        when(customerRepository.findById(123L)).thenReturn(Mono.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(registerCustomerCommandHandler.handle(kafkaCommand))
                .expectNextMatches(result ->
                        result.getCustomerId().equals(123L) &&
                        result.getName().equals("Kafka Customer"))
                .verifyComplete();
    }
}
