package com.nttdata.customer.client.application.update_customer;

import com.nttdata.customer.client.domain.Gender;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateCustomerCommand {

    private final Long customerId;
    private final String name;
    private final Gender gender;
    private final String address;
    private final String phone;
    private final String password;
    private final Boolean status;
}
