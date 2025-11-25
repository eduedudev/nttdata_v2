package com.nttdata.account.application.create_account;

import com.nttdata.account.domain.AccountType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CreateAccountCommand {

    private final String accountNumber;
    private final AccountType accountType;
    private final BigDecimal initialBalance;
    private final Boolean status;
    private final Long customerId;
}
