package com.nttdata.customer.client.application.update_customer;

import com.nttdata.customer.client.domain.Customer;
import com.nttdata.customer.client.domain.CustomerMother;
import com.nttdata.customer.client.domain.CustomerNotFoundException;
import com.nttdata.customer.client.domain.CustomerRepository;
import com.nttdata.customer.client.domain.Gender;
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
class UpdateCustomerCommandHandlerImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private UpdateCustomerCommandHandlerImpl updateCustomerCommandHandler;

    private UpdateCustomerCommand command;
    private Customer existingCustomer;

    @BeforeEach
    void setUp() {
        command = UpdateCustomerCommandMother.createDefault();
        existingCustomer = CustomerMother.createDefault();
    }

    @Test
    void shouldUpdateCustomerSuccessfully() {
        Customer updatedCustomer = CustomerMother.validCustomer()
                .name("John Doe Updated")
                .address("456 Updated Street")
                .phone("+573009999999")
                .password("newpassword123")
                .build();

        when(customerRepository.findById(anyLong())).thenReturn(Mono.just(existingCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(updatedCustomer));

        StepVerifier.create(updateCustomerCommandHandler.handle(command))
                .expectNextMatches(result ->
                        result.getName().equals("John Doe Updated") &&
                        result.getAddress().equals("456 Updated Street") &&
                        result.getPhone().equals("+573009999999"))
                .verifyComplete();

        verify(customerRepository).findById(1L);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {
        when(customerRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(updateCustomerCommandHandler.handle(command))
                .expectError(CustomerNotFoundException.class)
                .verify();

        verify(customerRepository).findById(1L);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void shouldPreserveIdentificationWhenUpdating() {
        when(customerRepository.findById(anyLong())).thenReturn(Mono.just(existingCustomer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            return Mono.just(c);
        });

        StepVerifier.create(updateCustomerCommandHandler.handle(command))
                .expectNextMatches(result ->
                        result.getIdentification().equals("1234567890"))
                .verifyComplete();
    }

    @Test
    void shouldPreserveCreatedAtWhenUpdating() {
        when(customerRepository.findById(anyLong())).thenReturn(Mono.just(existingCustomer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            return Mono.just(c);
        });

        StepVerifier.create(updateCustomerCommandHandler.handle(command))
                .expectNextMatches(result ->
                        result.getCreatedAt() != null &&
                        result.getCreatedAt().equals(existingCustomer.getCreatedAt()))
                .verifyComplete();
    }

    @Test
    void shouldUpdateUpdatedAtWhenUpdating() {
        when(customerRepository.findById(anyLong())).thenReturn(Mono.just(existingCustomer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            return Mono.just(c);
        });

        StepVerifier.create(updateCustomerCommandHandler.handle(command))
                .expectNextMatches(result ->
                        result.getUpdatedAt() != null &&
                        !result.getUpdatedAt().equals(existingCustomer.getUpdatedAt()))
                .verifyComplete();
    }

    @Test
    void shouldUpdateCustomerToInactive() {
        UpdateCustomerCommand inactiveCommand = UpdateCustomerCommandMother.createInactive();
        Customer inactiveCustomer = CustomerMother.createInactive();

        when(customerRepository.findById(anyLong())).thenReturn(Mono.just(existingCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(inactiveCustomer));

        StepVerifier.create(updateCustomerCommandHandler.handle(inactiveCommand))
                .expectNextMatches(result -> !result.getStatus())
                .verifyComplete();
    }

    @Test
    void shouldUpdateCustomerGender() {
        UpdateCustomerCommand femaleCommand = UpdateCustomerCommandMother.createFemale();
        Customer femaleCustomer = CustomerMother.validCustomer()
                .name("Jane Doe Updated")
                .gender(Gender.FEMALE)
                .build();

        when(customerRepository.findById(anyLong())).thenReturn(Mono.just(existingCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(femaleCustomer));

        StepVerifier.create(updateCustomerCommandHandler.handle(femaleCommand))
                .expectNextMatches(result ->
                        result.getName().equals("Jane Doe Updated") &&
                        result.getGender() == Gender.FEMALE)
                .verifyComplete();
    }

    @Test
    void shouldUpdateCustomerWithDifferentId() {
        UpdateCustomerCommand customCommand = UpdateCustomerCommandMother.createWithCustomerId(99L);
        Customer customer99 = CustomerMother.validCustomer().customerId(99L).build();

        when(customerRepository.findById(99L)).thenReturn(Mono.just(customer99));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            return Mono.just(c);
        });

        StepVerifier.create(updateCustomerCommandHandler.handle(customCommand))
                .expectNextMatches(result -> result.getCustomerId().equals(99L))
                .verifyComplete();

        verify(customerRepository).findById(99L);
    }
}
