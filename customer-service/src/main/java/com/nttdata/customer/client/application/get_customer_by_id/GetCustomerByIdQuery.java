package com.nttdata.customer.client.application.get_customer_by_id;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetCustomerByIdQuery {

    private final Long customerId;
}
