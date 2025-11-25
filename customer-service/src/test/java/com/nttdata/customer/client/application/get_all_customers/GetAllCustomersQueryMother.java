package com.nttdata.customer.client.application.get_all_customers;

public class GetAllCustomersQueryMother {

    public static GetAllCustomersQuery createDefault() {
        return GetAllCustomersQuery.builder()
                .page(0)
                .size(20)
                .build();
    }

    public static GetAllCustomersQuery createWithPagination(Integer page, Integer size) {
        return GetAllCustomersQuery.builder()
                .page(page)
                .size(size)
                .build();
    }

    public static GetAllCustomersQuery createWithNullValues() {
        return GetAllCustomersQuery.builder()
                .page(null)
                .size(null)
                .build();
    }

    public static GetAllCustomersQuery createSecondPage() {
        return GetAllCustomersQuery.builder()
                .page(1)
                .size(10)
                .build();
    }

    public static GetAllCustomersQuery createSmallPage() {
        return GetAllCustomersQuery.builder()
                .page(0)
                .size(5)
                .build();
    }
}
