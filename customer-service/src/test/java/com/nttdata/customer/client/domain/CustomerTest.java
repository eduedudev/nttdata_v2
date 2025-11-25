package com.nttdata.customer.client.domain;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    void shouldCreateCustomerWithAllFields() {
        Customer customer = CustomerMother.createDefault();

        assertNotNull(customer);
        assertEquals(1L, customer.getCustomerId());
        assertEquals("John Doe", customer.getName());
        assertEquals(Gender.MALE, customer.getGender());
        assertEquals("1234567890", customer.getIdentification());
        assertEquals("123 Main Street", customer.getAddress());
        assertEquals("+573001234567", customer.getPhone());
        assertEquals("password123", customer.getPassword());
        assertTrue(customer.getStatus());
        assertNotNull(customer.getCreatedAt());
        assertNotNull(customer.getUpdatedAt());
    }

    @Test
    void shouldVerifyCustomerExtendsPerson() {
        Customer customer = CustomerMother.createDefault();

        assertTrue(customer instanceof Person);
    }

    @Test
    void shouldCreateCustomerWithSpecificId() {
        Customer customer = CustomerMother.createWithId(99L);

        assertEquals(99L, customer.getCustomerId());
    }

    @Test
    void shouldCreateCustomerWithSpecificIdentification() {
        Customer customer = CustomerMother.createWithIdentification("9999999999");

        assertEquals("9999999999", customer.getIdentification());
    }

    @Test
    void shouldCreateCustomerWithoutId() {
        Customer customer = CustomerMother.createWithoutId();

        assertNull(customer.getCustomerId());
    }

    @Test
    void shouldCreateInactiveCustomer() {
        Customer customer = CustomerMother.createInactive();

        assertFalse(customer.getStatus());
    }

    @Test
    void shouldCreateFemaleCustomer() {
        Customer customer = CustomerMother.createFemale();

        assertEquals("Jane Doe", customer.getName());
        assertEquals(Gender.FEMALE, customer.getGender());
        assertEquals("0987654321", customer.getIdentification());
    }

    @Test
    void shouldCreateCustomerWithCustomDates() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2025-01-01T00:00:00Z");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2025-06-15T12:00:00Z");

        Customer customer = CustomerMother.createWithDates(createdAt, updatedAt);

        assertEquals(createdAt, customer.getCreatedAt());
        assertEquals(updatedAt, customer.getUpdatedAt());
    }

    @Test
    void shouldCreateCustomerWithoutDates() {
        Customer customer = CustomerMother.createWithoutDates();

        assertNull(customer.getCreatedAt());
        assertNull(customer.getUpdatedAt());
    }

    @Test
    void shouldAllowSettersToModifyCustomer() {
        Customer customer = new Customer();
        OffsetDateTime now = OffsetDateTime.now();

        customer.setCustomerId(100L);
        customer.setName("Modified Name");
        customer.setPassword("newPassword");
        customer.setStatus(false);
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);

        assertEquals(100L, customer.getCustomerId());
        assertEquals("Modified Name", customer.getName());
        assertEquals("newPassword", customer.getPassword());
        assertFalse(customer.getStatus());
        assertEquals(now, customer.getCreatedAt());
        assertEquals(now, customer.getUpdatedAt());
    }
}
