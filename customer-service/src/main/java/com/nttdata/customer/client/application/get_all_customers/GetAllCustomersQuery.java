package com.nttdata.customer.client.application.get_all_customers;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetAllCustomersQuery {

    private final Integer page;
    private final Integer size;

    public int getPageOrDefault() {
        return page != null ? page : 0;
    }

    public int getSizeOrDefault() {
        return size != null ? size : 20;
    }
}
