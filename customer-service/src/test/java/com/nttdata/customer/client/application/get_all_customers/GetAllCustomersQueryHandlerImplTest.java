package com.nttdata.customer.client.application.get_all_customers;

import com.nttdata.customer.client.domain.Customer;
import com.nttdata.customer.client.domain.CustomerMother;
import com.nttdata.customer.client.domain.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllCustomersQueryHandlerImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private GetAllCustomersQueryHandlerImpl getAllCustomersQueryHandler;

    private GetAllCustomersQuery query;

    @BeforeEach
    void setUp() {
        query = GetAllCustomersQueryMother.createDefault();
    }

    @Test
    void shouldReturnAllCustomers() {
        Customer customer1 = CustomerMother.validCustomer().customerId(1L).identification("ID001").build();
        Customer customer2 = CustomerMother.validCustomer().customerId(2L).identification("ID002").build();
        Customer customer3 = CustomerMother.validCustomer().customerId(3L).identification("ID003").build();

        when(customerRepository.findAll()).thenReturn(Flux.just(customer1, customer2, customer3));

        StepVerifier.create(getAllCustomersQueryHandler.handle(query))
                .expectNext(customer1)
                .expectNext(customer2)
                .expectNext(customer3)
                .verifyComplete();

        verify(customerRepository).findAll();
    }

    @Test
    void shouldReturnEmptyFluxWhenNoCustomers() {
        when(customerRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(getAllCustomersQueryHandler.handle(query))
                .verifyComplete();

        verify(customerRepository).findAll();
    }

    @Test
    void shouldApplyPagination() {
        GetAllCustomersQuery paginatedQuery = GetAllCustomersQueryMother.createSmallPage();
        
        Customer customer1 = CustomerMother.validCustomer().customerId(1L).identification("ID001").build();
        Customer customer2 = CustomerMother.validCustomer().customerId(2L).identification("ID002").build();
        Customer customer3 = CustomerMother.validCustomer().customerId(3L).identification("ID003").build();
        Customer customer4 = CustomerMother.validCustomer().customerId(4L).identification("ID004").build();
        Customer customer5 = CustomerMother.validCustomer().customerId(5L).identification("ID005").build();
        Customer customer6 = CustomerMother.validCustomer().customerId(6L).identification("ID006").build();

        when(customerRepository.findAll()).thenReturn(Flux.just(customer1, customer2, customer3, customer4, customer5, customer6));

        StepVerifier.create(getAllCustomersQueryHandler.handle(paginatedQuery))
                .expectNext(customer1)
                .expectNext(customer2)
                .expectNext(customer3)
                .expectNext(customer4)
                .expectNext(customer5)
                .verifyComplete();

        verify(customerRepository).findAll();
    }

    @Test
    void shouldReturnSecondPage() {
        GetAllCustomersQuery secondPageQuery = GetAllCustomersQueryMother.createWithPagination(1, 2);
        
        Customer customer1 = CustomerMother.validCustomer().customerId(1L).identification("ID001").build();
        Customer customer2 = CustomerMother.validCustomer().customerId(2L).identification("ID002").build();
        Customer customer3 = CustomerMother.validCustomer().customerId(3L).identification("ID003").build();
        Customer customer4 = CustomerMother.validCustomer().customerId(4L).identification("ID004").build();

        when(customerRepository.findAll()).thenReturn(Flux.just(customer1, customer2, customer3, customer4));

        StepVerifier.create(getAllCustomersQueryHandler.handle(secondPageQuery))
                .expectNext(customer3)
                .expectNext(customer4)
                .verifyComplete();

        verify(customerRepository).findAll();
    }

    @Test
    void shouldUseDefaultValuesWhenNullPagination() {
        GetAllCustomersQuery nullQuery = GetAllCustomersQueryMother.createWithNullValues();
        
        Customer customer1 = CustomerMother.validCustomer().customerId(1L).identification("ID001").build();

        when(customerRepository.findAll()).thenReturn(Flux.just(customer1));

        StepVerifier.create(getAllCustomersQueryHandler.handle(nullQuery))
                .expectNext(customer1)
                .verifyComplete();

        verify(customerRepository).findAll();
    }

    @Test
    void shouldReturnSingleCustomer() {
        Customer customer = CustomerMother.createDefault();

        when(customerRepository.findAll()).thenReturn(Flux.just(customer));

        StepVerifier.create(getAllCustomersQueryHandler.handle(query))
                .expectNextMatches(result -> 
                        result.getCustomerId().equals(1L) &&
                        result.getName().equals("John Doe"))
                .verifyComplete();
    }
}
