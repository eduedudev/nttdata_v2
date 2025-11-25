package com.nttdata.customer.client.domain;

import java.time.OffsetDateTime;

public class CustomerMother {

    private static final OffsetDateTime DEFAULT_DATE = OffsetDateTime.parse("2025-11-24T10:00:00Z");

    public static Customer.CustomerBuilder<?, ?> validCustomer() {
        return Customer.builder()
                .customerId(1L)
                .name("John Doe")
                .gender(Gender.MALE)
                .identification("1234567890")
                .address("123 Main Street")
                .phone("+573001234567")
                .password("password123")
                .status(true)
                .createdAt(DEFAULT_DATE)
                .updatedAt(DEFAULT_DATE);
    }

    public static Customer createDefault() {
        return validCustomer().build();
    }

    public static Customer createWithId(Long id) {
        return validCustomer().customerId(id).build();
    }

    public static Customer createWithIdentification(String identification) {
        return validCustomer().identification(identification).build();
    }

    public static Customer createWithoutId() {
        return validCustomer().customerId(null).build();
    }

    public static Customer createInactive() {
        return validCustomer().status(false).build();
    }

    public static Customer createFemale() {
        return validCustomer()
                .name("Jane Doe")
                .gender(Gender.FEMALE)
                .identification("0987654321")
                .build();
    }

    public static Customer createWithName(String name) {
        return validCustomer().name(name).build();
    }

    public static Customer createWithDates(OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        return validCustomer()
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Customer createWithoutDates() {
        return validCustomer()
                .createdAt(null)
                .updatedAt(null)
                .build();
    }

    public static Customer random() {
        return createDefault();
    }

    public static Customer withId(Long id) {
        return createWithId(id);
    }
}
