package com.nttdata.customer.client.domain;

import java.time.Instant;

public class CustomerCreatedEventMother {

    public static CustomerCreatedEvent random() {
        Customer customer = CustomerMother.random();
        return CustomerCreatedEvent.fromCustomer(customer);
    }

    public static CustomerCreatedEvent fromCustomer(Customer customer) {
        return CustomerCreatedEvent.fromCustomer(customer);
    }

    public static CustomerCreatedEvent withCustomerId(Long customerId) {
        return CustomerCreatedEvent.builder()
                .aggregateId(String.valueOf(customerId))
                .name("John Doe")
                .identification("1234567890")
                .gender("MALE")
                .address("123 Main Street")
                .phone("+573001234567")
                .status(true)
                .build();
    }
}
