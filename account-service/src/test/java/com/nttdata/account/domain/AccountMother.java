package com.nttdata.account.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class AccountMother {

    private static final OffsetDateTime DEFAULT_DATE = OffsetDateTime.parse("2025-11-24T10:00:00Z");

    public static Account.AccountBuilder validAccount() {
        return Account.builder()
                .accountId(1L)
                .accountNumber("1234567890")
                .accountType(AccountType.SAVINGS)
                .initialBalance(BigDecimal.valueOf(1000))
                .currentBalance(BigDecimal.valueOf(1000))
                .status(true)
                .customerId(1L)
                .createdAt(DEFAULT_DATE)
                .updatedAt(DEFAULT_DATE);
    }

    public static Account createDefault() {
        return validAccount().build();
    }

    public static Account createWithId(Long id) {
        return validAccount().accountId(id).build();
    }

    public static Account createWithAccountNumber(String accountNumber) {
        return validAccount().accountNumber(accountNumber).build();
    }

    public static Account createWithoutId() {
        return validAccount().accountId(null).build();
    }

    public static Account createInactive() {
        return validAccount().status(false).build();
    }

    public static Account createCheckingAccount() {
        return validAccount()
                .accountType(AccountType.CHECKING)
                .accountNumber("9876543210")
                .build();
    }

    public static Account createWithBalance(BigDecimal balance) {
        return validAccount()
                .initialBalance(balance)
                .currentBalance(balance)
                .build();
    }

    public static Account createWithCustomerId(Long customerId) {
        return validAccount().customerId(customerId).build();
    }

    public static Account createWithZeroBalance() {
        return validAccount()
                .initialBalance(BigDecimal.ZERO)
                .currentBalance(BigDecimal.ZERO)
                .build();
    }
}
