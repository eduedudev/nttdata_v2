package com.nttdata.customer.client.application;

import com.nttdata.customer.api.model.CustomerRequest;
import com.nttdata.customer.api.model.CustomerResponse;
import com.nttdata.customer.client.application.create_customer.CreateCustomerCommand;
import com.nttdata.customer.client.application.update_customer.UpdateCustomerCommand;
import com.nttdata.customer.client.domain.Customer;
import com.nttdata.customer.client.domain.CustomerMother;
import com.nttdata.customer.client.domain.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerMapperTest {

    private CustomerMapper customerMapper;

    @BeforeEach
    void setUp() {
        customerMapper = new CustomerMapper();
    }

    @Test
    void shouldMapCustomerRequestToCommand() {
        CustomerRequest request = CustomerRequestMother.createDefault();

        CreateCustomerCommand command = customerMapper.toCommand(request);

        assertNotNull(command);
        assertEquals("John Doe", command.getName());
        assertEquals(Gender.MALE, command.getGender());
        assertEquals("1234567890", command.getIdentification());
        assertEquals("123 Main Street", command.getAddress());
        assertEquals("+573001234567", command.getPhone());
        assertEquals("password123", command.getPassword());
        assertTrue(command.getStatus());
    }

    @Test
    void shouldMapCustomerToResponse() {
        Customer customer = CustomerMother.createDefault();

        CustomerResponse response = customerMapper.toResponse(customer);

        assertNotNull(response);
        assertEquals(1L, response.getCustomerId());
        assertEquals("John Doe", response.getName());
        assertEquals(CustomerResponse.GenderEnum.MALE, response.getGender());
        assertEquals("1234567890", response.getIdentification());
        assertEquals("123 Main Street", response.getAddress());
        assertEquals("+573001234567", response.getPhone());
        assertTrue(response.getStatus());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());
    }

    @Test
    void shouldMapFemaleGenderFromRequest() {
        CustomerRequest request = CustomerRequestMother.createFemale();

        CreateCustomerCommand command = customerMapper.toCommand(request);

        assertEquals(Gender.FEMALE, command.getGender());
        assertEquals("Jane Doe", command.getName());
    }

    @Test
    void shouldMapOtherGenderFromRequest() {
        CustomerRequest request = CustomerRequestMother.createOtherGender();

        CreateCustomerCommand command = customerMapper.toCommand(request);

        assertEquals(Gender.OTHER, command.getGender());
    }

    @Test
    void shouldHandleNullGenderInRequest() {
        CustomerRequest request = CustomerRequestMother.createWithNullGender();

        CreateCustomerCommand command = customerMapper.toCommand(request);

        assertNull(command.getGender());
    }

    @Test
    void shouldHandleNullGenderInCustomer() {
        Customer customer = CustomerMother.validCustomer().gender(null).build();

        CustomerResponse response = customerMapper.toResponse(customer);

        assertNull(response.getGender());
    }

    @Test
    void shouldMapInactiveCustomer() {
        CustomerRequest request = CustomerRequestMother.createInactive();

        CreateCustomerCommand command = customerMapper.toCommand(request);

        assertFalse(command.getStatus());
    }

    @Test
    void shouldMapFemaleGenderToResponse() {
        Customer customer = CustomerMother.createFemale();

        CustomerResponse response = customerMapper.toResponse(customer);

        assertEquals(CustomerResponse.GenderEnum.FEMALE, response.getGender());
    }

    @Test
    void shouldMapOtherGenderToResponse() {
        Customer customer = CustomerMother.validCustomer().gender(Gender.OTHER).build();

        CustomerResponse response = customerMapper.toResponse(customer);

        assertEquals(CustomerResponse.GenderEnum.OTHER, response.getGender());
    }

    @Test
    void shouldMapCustomerWithoutDates() {
        Customer customer = CustomerMother.createWithoutDates();

        CustomerResponse response = customerMapper.toResponse(customer);

        assertNull(response.getCreatedAt());
        assertNull(response.getUpdatedAt());
    }

    @Test
    void shouldMapCustomerRequestToUpdateCommand() {
        CustomerRequest request = CustomerRequestMother.createDefault();
        Long customerId = 1L;

        UpdateCustomerCommand command = customerMapper.toUpdateCommand(customerId, request);

        assertNotNull(command);
        assertEquals(1L, command.getCustomerId());
        assertEquals("John Doe", command.getName());
        assertEquals(Gender.MALE, command.getGender());
        assertEquals("123 Main Street", command.getAddress());
        assertEquals("+573001234567", command.getPhone());
        assertEquals("password123", command.getPassword());
        assertTrue(command.getStatus());
    }

    @Test
    void shouldMapUpdateCommandWithDifferentCustomerId() {
        CustomerRequest request = CustomerRequestMother.createDefault();
        Long customerId = 99L;

        UpdateCustomerCommand command = customerMapper.toUpdateCommand(customerId, request);

        assertEquals(99L, command.getCustomerId());
    }

    @Test
    void shouldMapUpdateCommandWithFemaleGender() {
        CustomerRequest request = CustomerRequestMother.createFemale();
        Long customerId = 1L;

        UpdateCustomerCommand command = customerMapper.toUpdateCommand(customerId, request);

        assertEquals(Gender.FEMALE, command.getGender());
    }

    @Test
    void shouldMapUpdateCommandWithInactiveStatus() {
        CustomerRequest request = CustomerRequestMother.createInactive();
        Long customerId = 1L;

        UpdateCustomerCommand command = customerMapper.toUpdateCommand(customerId, request);

        assertFalse(command.getStatus());
    }
}
