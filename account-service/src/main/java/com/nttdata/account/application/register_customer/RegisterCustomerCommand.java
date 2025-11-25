package com.nttdata.account.application.register_customer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterCustomerCommand {

    private final Long customerId;
    private final String name;
    private final String identification;
    private final String address;
    private final String phone;
    private final Boolean status;
}
