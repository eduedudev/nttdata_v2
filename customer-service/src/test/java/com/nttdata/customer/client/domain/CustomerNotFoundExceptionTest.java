package com.nttdata.customer.client.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerNotFoundExceptionTest {

    @Test
    void shouldCreateExceptionWithId() {
        Long id = 1L;

        CustomerNotFoundException exception = new CustomerNotFoundException(id);

        assertNotNull(exception);
        assertEquals("Customer with id 1 not found", exception.getMessage());
    }

    @Test
    void shouldCreateExceptionWithIdentification() {
        String identification = "1234567890";

        CustomerNotFoundException exception = new CustomerNotFoundException(identification);

        assertNotNull(exception);
        assertEquals("Customer with identification 1234567890 not found", exception.getMessage());
    }

    @Test
    void shouldCreateExceptionWithDifferentId() {
        Long id = 999L;

        CustomerNotFoundException exception = new CustomerNotFoundException(id);

        assertEquals("Customer with id 999 not found", exception.getMessage());
    }
}
