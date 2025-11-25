package com.nttdata.customer.client.application.get_customer_by_id;

public class GetCustomerByIdQueryMother {

    public static GetCustomerByIdQuery createDefault() {
        return GetCustomerByIdQuery.builder()
                .customerId(1L)
                .build();
    }

    public static GetCustomerByIdQuery createWithCustomerId(Long customerId) {
        return GetCustomerByIdQuery.builder()
                .customerId(customerId)
                .build();
    }
}
