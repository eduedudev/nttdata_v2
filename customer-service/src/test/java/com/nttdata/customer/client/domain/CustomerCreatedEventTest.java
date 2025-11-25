package com.nttdata.customer.client.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomerCreatedEventTest {

    @Test
    void shouldCreateEventFromCustomer() {
        Customer customer = CustomerMother.withId(1L);

        CustomerCreatedEvent event = CustomerCreatedEvent.fromCustomer(customer);

        assertNotNull(event.getEventId());
        assertEquals("CustomerCreated", event.getEventType());
        assertNotNull(event.getOccurredOn());
        assertEquals("1", event.getAggregateId());
        assertEquals(customer.getName(), event.getName());
        assertEquals(customer.getIdentification(), event.getIdentification());
        assertEquals(customer.getGender().name(), event.getGender());
        assertEquals(customer.getAddress(), event.getAddress());
        assertEquals(customer.getPhone(), event.getPhone());
        assertEquals(customer.getStatus(), event.getStatus());
    }

    @Test
    void shouldHaveCorrectEventType() {
        CustomerCreatedEvent event = CustomerCreatedEventMother.random();

        assertEquals("CustomerCreated", event.getEventType());
    }

    @Test
    void shouldHaveOccurredOnTimestamp() {
        Instant before = Instant.now();

        CustomerCreatedEvent event = CustomerCreatedEventMother.random();

        Instant after = Instant.now();
        assertNotNull(event.getOccurredOn());
        assertEquals(true, !event.getOccurredOn().isBefore(before));
        assertEquals(true, !event.getOccurredOn().isAfter(after));
    }

    @Test
    void shouldGenerateUniqueEventId() {
        CustomerCreatedEvent event1 = CustomerCreatedEventMother.random();
        CustomerCreatedEvent event2 = CustomerCreatedEventMother.random();

        assertNotNull(event1.getEventId());
        assertNotNull(event2.getEventId());
        assertEquals(false, event1.getEventId().equals(event2.getEventId()));
    }
}
