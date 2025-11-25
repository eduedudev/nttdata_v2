package com.nttdata.account.application.update_account;

import com.nttdata.account.domain.AccountType;

import java.math.BigDecimal;

public class UpdateAccountCommandMother {

    public static UpdateAccountCommand.UpdateAccountCommandBuilder validCommand() {
        return UpdateAccountCommand.builder()
                .accountId(1L)
                .accountNumber("1234567890")
                .accountType(AccountType.SAVINGS)
                .initialBalance(BigDecimal.valueOf(1000))
                .status(true);
    }

    public static UpdateAccountCommand createDefault() {
        return validCommand().build();
    }

    public static UpdateAccountCommand createWithId(Long accountId) {
        return validCommand().accountId(accountId).build();
    }

    public static UpdateAccountCommand createWithAccountNumber(String accountNumber) {
        return validCommand().accountNumber(accountNumber).build();
    }

    public static UpdateAccountCommand createCheckingAccount() {
        return validCommand()
                .accountType(AccountType.CHECKING)
                .build();
    }

    public static UpdateAccountCommand createInactive() {
        return validCommand().status(false).build();
    }

    public static UpdateAccountCommand createWithNewBalance(BigDecimal initialBalance) {
        return validCommand().initialBalance(initialBalance).build();
    }
}
