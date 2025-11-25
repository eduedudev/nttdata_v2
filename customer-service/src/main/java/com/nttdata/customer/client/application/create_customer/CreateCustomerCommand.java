package com.nttdata.customer.client.application.create_customer;

import com.nttdata.customer.client.domain.Gender;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateCustomerCommand {

    private final String name;
    private final Gender gender;
    private final String identification;
    private final String address;
    private final String phone;
    private final String password;
    private final Boolean status;
}
