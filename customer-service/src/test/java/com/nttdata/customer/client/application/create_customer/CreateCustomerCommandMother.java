package com.nttdata.customer.client.application.create_customer;

import com.nttdata.customer.client.domain.Gender;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateCustomerCommandMother {

    public static CreateCustomerCommand.CreateCustomerCommandBuilder validCommand() {
        return CreateCustomerCommand.builder()
                .name("John Doe")
                .gender(Gender.MALE)
                .identification("1234567890")
                .address("123 Main Street")
                .phone("+573001234567")
                .password("password123")
                .status(true);
    }

    public static CreateCustomerCommand createDefault() {
        return validCommand().build();
    }

    public static CreateCustomerCommand createWithIdentification(String identification) {
        return validCommand().identification(identification).build();
    }

    public static CreateCustomerCommand createFemale() {
        return validCommand()
                .name("Jane Doe")
                .gender(Gender.FEMALE)
                .identification("0987654321")
                .build();
    }

    public static CreateCustomerCommand createInactive() {
        return validCommand().status(false).build();
    }
}
