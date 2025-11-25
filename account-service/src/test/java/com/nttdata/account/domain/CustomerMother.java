package com.nttdata.account.domain;

public class CustomerMother {

    public static Customer.CustomerBuilder validCustomer() {
        return Customer.builder()
                .customerId(1L)
                .name("John Doe")
                .identification("1234567890")
                .address("123 Main Street")
                .phone("+573001234567")
                .status(true);
    }

    public static Customer createDefault() {
        return validCustomer().build();
    }

    public static Customer createWithId(Long id) {
        return validCustomer().customerId(id).build();
    }

    public static Customer createWithName(String name) {
        return validCustomer().name(name).build();
    }

    public static Customer createInactive() {
        return validCustomer().status(false).build();
    }
}
