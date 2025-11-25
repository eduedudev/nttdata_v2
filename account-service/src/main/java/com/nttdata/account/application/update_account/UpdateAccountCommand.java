package com.nttdata.account.application.update_account;

import com.nttdata.account.domain.AccountType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class UpdateAccountCommand {

    private final Long accountId;
    private final String accountNumber;
    private final AccountType accountType;
    private final BigDecimal initialBalance;
    private final Boolean status;
}
