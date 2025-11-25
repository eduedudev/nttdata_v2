package com.nttdata.account.application.register_customer;

public class RegisterCustomerCommandMother {

    public static RegisterCustomerCommand.RegisterCustomerCommandBuilder validCommand() {
        return RegisterCustomerCommand.builder()
                .customerId(1L)
                .name("John Doe")
                .identification("1234567890")
                .address("123 Main Street")
                .phone("+573001234567")
                .status(true);
    }

    public static RegisterCustomerCommand createDefault() {
        return validCommand().build();
    }

    public static RegisterCustomerCommand createWithId(Long customerId) {
        return validCommand().customerId(customerId).build();
    }

    public static RegisterCustomerCommand createWithName(String name) {
        return validCommand().name(name).build();
    }

    public static RegisterCustomerCommand createInactive() {
        return validCommand().status(false).build();
    }

    public static RegisterCustomerCommand createWithIdentification(String identification) {
        return validCommand().identification(identification).build();
    }
}
