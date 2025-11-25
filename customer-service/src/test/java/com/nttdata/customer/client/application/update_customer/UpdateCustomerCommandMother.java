package com.nttdata.customer.client.application.update_customer;

import com.nttdata.customer.client.domain.Gender;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateCustomerCommandMother {

    public static UpdateCustomerCommand.UpdateCustomerCommandBuilder validCommand() {
        return UpdateCustomerCommand.builder()
                .customerId(1L)
                .name("John Doe Updated")
                .gender(Gender.MALE)
                .address("456 Updated Street")
                .phone("+573009999999")
                .password("newpassword123")
                .status(true);
    }

    public static UpdateCustomerCommand createDefault() {
        return validCommand().build();
    }

    public static UpdateCustomerCommand createWithCustomerId(Long customerId) {
        return validCommand().customerId(customerId).build();
    }

    public static UpdateCustomerCommand createFemale() {
        return validCommand()
                .name("Jane Doe Updated")
                .gender(Gender.FEMALE)
                .build();
    }

    public static UpdateCustomerCommand createInactive() {
        return validCommand().status(false).build();
    }

    public static UpdateCustomerCommand createWithNewAddress(String address) {
        return validCommand().address(address).build();
    }
}
