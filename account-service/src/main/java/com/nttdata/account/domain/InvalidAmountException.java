package com.nttdata.account.domain;

import java.math.BigDecimal;

public class InvalidAmountException extends RuntimeException {

    private final BigDecimal amount;

    public InvalidAmountException(BigDecimal amount) {
        super("The movement amount must be greater than zero");
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
