package com.nttdata.customer.client.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerAlreadyExistsExceptionTest {

    @Test
    void shouldCreateExceptionWithIdentification() {
        String identification = "1234567890";

        CustomerAlreadyExistsException exception = new CustomerAlreadyExistsException(identification);

        assertNotNull(exception);
        assertEquals("Customer with identification 1234567890 already exists", exception.getMessage());
    }

    @Test
    void shouldCreateExceptionWithDifferentIdentification() {
        String identification = "0987654321";

        CustomerAlreadyExistsException exception = new CustomerAlreadyExistsException(identification);

        assertEquals("Customer with identification 0987654321 already exists", exception.getMessage());
    }
}
