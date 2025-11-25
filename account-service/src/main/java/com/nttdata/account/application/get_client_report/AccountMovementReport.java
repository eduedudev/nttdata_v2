package com.nttdata.account.application.get_client_report;

import com.nttdata.account.domain.AccountType;
import com.nttdata.account.domain.MovementType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class AccountMovementReport {

    private final Long accountId;
    private final OffsetDateTime date;
    private final String customerName;
    private final String accountNumber;
    private final AccountType accountType;
    private final BigDecimal initialBalance;
    private final Boolean accountStatus;
    private final MovementType movementType;
    private final BigDecimal movementAmount;
    private final BigDecimal availableBalance;
}
