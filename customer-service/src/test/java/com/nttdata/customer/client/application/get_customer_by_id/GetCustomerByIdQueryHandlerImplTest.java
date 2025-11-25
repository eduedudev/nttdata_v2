package com.nttdata.customer.client.application.get_customer_by_id;

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
class GetCustomerByIdQueryHandlerImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private GetCustomerByIdQueryHandlerImpl getCustomerByIdQueryHandler;

    private GetCustomerByIdQuery query;
    private Customer existingCustomer;

    @BeforeEach
    void setUp() {
        query = GetCustomerByIdQueryMother.createDefault();
        existingCustomer = CustomerMother.createDefault();
    }

    @Test
    void shouldReturnCustomerById() {
        when(customerRepository.findById(anyLong())).thenReturn(Mono.just(existingCustomer));

        StepVerifier.create(getCustomerByIdQueryHandler.handle(query))
                .expectNextMatches(result ->
                        result.getCustomerId().equals(1L) &&
                        result.getName().equals("John Doe") &&
                        result.getIdentification().equals("1234567890"))
                .verifyComplete();

        verify(customerRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {
        when(customerRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(getCustomerByIdQueryHandler.handle(query))
                .expectError(CustomerNotFoundException.class)
                .verify();

        verify(customerRepository).findById(1L);
    }

    @Test
    void shouldReturnCustomerWithDifferentId() {
        GetCustomerByIdQuery customQuery = GetCustomerByIdQueryMother.createWithCustomerId(99L);
        Customer customer99 = CustomerMother.validCustomer()
                .customerId(99L)
                .name("Jane Doe")
                .identification("9999999999")
                .build();

        when(customerRepository.findById(99L)).thenReturn(Mono.just(customer99));

        StepVerifier.create(getCustomerByIdQueryHandler.handle(customQuery))
                .expectNextMatches(result ->
                        result.getCustomerId().equals(99L) &&
                        result.getName().equals("Jane Doe") &&
                        result.getIdentification().equals("9999999999"))
                .verifyComplete();

        verify(customerRepository).findById(99L);
    }

    @Test
    void shouldReturnCustomerWithAllFields() {
        Customer fullCustomer = CustomerMother.createDefault();

        when(customerRepository.findById(anyLong())).thenReturn(Mono.just(fullCustomer));

        StepVerifier.create(getCustomerByIdQueryHandler.handle(query))
                .expectNextMatches(result ->
                        result.getCustomerId() != null &&
                        result.getName() != null &&
                        result.getGender() != null &&
                        result.getIdentification() != null &&
                        result.getAddress() != null &&
                        result.getPhone() != null &&
                        result.getPassword() != null &&
                        result.getStatus() != null &&
                        result.getCreatedAt() != null &&
                        result.getUpdatedAt() != null)
                .verifyComplete();
    }

    @Test
    void shouldNotFindNonExistentCustomer() {
        GetCustomerByIdQuery nonExistentQuery = GetCustomerByIdQueryMother.createWithCustomerId(999L);

        when(customerRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(getCustomerByIdQueryHandler.handle(nonExistentQuery))
                .expectError(CustomerNotFoundException.class)
                .verify();

        verify(customerRepository).findById(999L);
    }
}
