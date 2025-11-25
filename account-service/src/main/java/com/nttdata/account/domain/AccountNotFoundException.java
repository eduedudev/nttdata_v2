package com.nttdata.account.domain;

public class AccountNotFoundException extends RuntimeException {

    private final Long accountId;

    public AccountNotFoundException(Long accountId) {
        super(String.format("Account not found with id: %d", accountId));
        this.accountId = accountId;
    }

    public Long getAccountId() {
        return accountId;
    }
}
