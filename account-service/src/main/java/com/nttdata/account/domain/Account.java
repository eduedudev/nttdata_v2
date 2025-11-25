package com.nttdata.account.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    private Long accountId;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal initialBalance;
    private BigDecimal currentBalance;
    private Boolean status;
    private Long customerId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Account credit(BigDecimal amount) {
        this.currentBalance = this.currentBalance.add(amount);
        this.updatedAt = OffsetDateTime.now();
        return this;
    }

    public Account debit(BigDecimal amount) {
        if (this.currentBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(this.accountId, this.currentBalance, amount);
        }
        this.currentBalance = this.currentBalance.subtract(amount);
        this.updatedAt = OffsetDateTime.now();
        return this;
    }

    public boolean hasInsufficientBalance(BigDecimal amount) {
        return this.currentBalance.compareTo(amount) < 0;
    }
}
