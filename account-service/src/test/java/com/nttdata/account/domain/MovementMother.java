package com.nttdata.account.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class MovementMother {

    private static final OffsetDateTime DEFAULT_DATE = OffsetDateTime.parse("2025-11-24T10:00:00Z");

    public static Movement.MovementBuilder validMovement() {
        return Movement.builder()
                .movementId(1L)
                .accountId(1L)
                .movementType(MovementType.CREDIT)
                .amount(BigDecimal.valueOf(500))
                .balance(BigDecimal.valueOf(1500))
                .date(DEFAULT_DATE)
                .description("Test movement");
    }

    public static Movement createDefault() {
        return validMovement().build();
    }

    public static Movement createWithId(Long id) {
        return validMovement().movementId(id).build();
    }

    public static Movement createCredit(BigDecimal amount, BigDecimal balanceAfter) {
        return validMovement()
                .movementType(MovementType.CREDIT)
                .amount(amount)
                .balance(balanceAfter)
                .build();
    }

    public static Movement createDebit(BigDecimal amount, BigDecimal balanceAfter) {
        return validMovement()
                .movementType(MovementType.DEBIT)
                .amount(amount)
                .balance(balanceAfter)
                .build();
    }

    public static Movement createWithAccountId(Long accountId) {
        return validMovement().accountId(accountId).build();
    }

    public static Movement createWithAmount(BigDecimal amount) {
        return validMovement().amount(amount).build();
    }

    public static Movement createWithDescription(String description) {
        return validMovement().description(description).build();
    }

    public static Movement createWithDate(OffsetDateTime date) {
        return validMovement().date(date).build();
    }
}
