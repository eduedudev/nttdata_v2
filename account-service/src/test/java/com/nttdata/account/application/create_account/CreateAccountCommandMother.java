package com.nttdata.account.application.create_account;

import com.nttdata.account.domain.AccountType;

import java.math.BigDecimal;

public class CreateAccountCommandMother {

    public static CreateAccountCommand.CreateAccountCommandBuilder validCommand() {
        return CreateAccountCommand.builder()
                .accountNumber("1234567890")
                .accountType(AccountType.SAVINGS)
                .initialBalance(BigDecimal.valueOf(1000))
                .status(true)
                .customerId(1L);
    }

    public static CreateAccountCommand createDefault() {
        return validCommand().build();
    }

    public static CreateAccountCommand createWithAccountNumber(String accountNumber) {
        return validCommand().accountNumber(accountNumber).build();
    }

    public static CreateAccountCommand createWithCustomerId(Long customerId) {
        return validCommand().customerId(customerId).build();
    }

    public static CreateAccountCommand createCheckingAccount() {
        return validCommand()
                .accountType(AccountType.CHECKING)
                .accountNumber("9876543210")
                .build();
    }

    public static CreateAccountCommand createWithInitialBalance(BigDecimal initialBalance) {
        return validCommand().initialBalance(initialBalance).build();
    }

    public static CreateAccountCommand createInactive() {
        return validCommand().status(false).build();
    }
}
