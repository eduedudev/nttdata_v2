package com.nttdata.customer.client.application.delete_customer;

import com.nttdata.customer.client.domain.Customer;
import com.nttdata.customer.client.domain.CustomerMother;
import com.nttdata.customer.client.domain.CustomerNotFoundException;
import com.nttdata.customer.client.domain.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteCustomerCommandHandlerImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private DeleteCustomerCommandHandlerImpl deleteCustomerCommandHandler;

    private DeleteCustomerCommand command;
    private Customer existingCustomer;

    @BeforeEach
    void setUp() {
        command = DeleteCustomerCommandMother.createDefault();
        existingCustomer = CustomerMother.createDefault();
    }

    @Test
    void shouldDeleteCustomerSuccessfully() {
        when(customerRepository.findById(anyLong())).thenReturn(Mono.just(existingCustomer));
        when(customerRepository.deleteById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(deleteCustomerCommandHandler.handle(command))
                .verifyComplete();

        verify(customerRepository).findById(1L);
        verify(customerRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {
        when(customerRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(deleteCustomerCommandHandler.handle(command))
                .expectError(CustomerNotFoundException.class)
                .verify();

        verify(customerRepository).findById(1L);
        verify(customerRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldDeleteCustomerWithDifferentId() {
        DeleteCustomerCommand customCommand = DeleteCustomerCommandMother.createWithCustomerId(99L);
        Customer customer99 = CustomerMother.validCustomer().customerId(99L).build();

        when(customerRepository.findById(99L)).thenReturn(Mono.just(customer99));
        when(customerRepository.deleteById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(deleteCustomerCommandHandler.handle(customCommand))
                .verifyComplete();

        verify(customerRepository).findById(99L);
        verify(customerRepository).deleteById(99L);
    }

    @Test
    void shouldNotDeleteWhenCustomerDoesNotExist() {
        DeleteCustomerCommand customCommand = DeleteCustomerCommandMother.createWithCustomerId(999L);

        when(customerRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(deleteCustomerCommandHandler.handle(customCommand))
                .expectError(CustomerNotFoundException.class)
                .verify();

        verify(customerRepository).findById(999L);
        verify(customerRepository, never()).deleteById(anyLong());
    }
}
