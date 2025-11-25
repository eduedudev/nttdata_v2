package com.nttdata.customer.client.application.delete_customer;

public class DeleteCustomerCommandMother {

    public static DeleteCustomerCommand createDefault() {
        return DeleteCustomerCommand.builder()
                .customerId(1L)
                .build();
    }

    public static DeleteCustomerCommand createWithCustomerId(Long customerId) {
        return DeleteCustomerCommand.builder()
                .customerId(customerId)
                .build();
    }
}
