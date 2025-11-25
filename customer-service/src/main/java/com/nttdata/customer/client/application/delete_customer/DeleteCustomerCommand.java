package com.nttdata.customer.client.application.delete_customer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeleteCustomerCommand {

    private final Long customerId;
}
