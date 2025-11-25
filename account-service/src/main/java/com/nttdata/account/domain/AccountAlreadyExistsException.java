package com.nttdata.account.domain;

public class AccountAlreadyExistsException extends RuntimeException {

    private final String accountNumber;

    public AccountAlreadyExistsException(String accountNumber) {
        super(String.format("Account already exists with number: %s", accountNumber));
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
