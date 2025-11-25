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
public class Movement {

    private Long movementId;
    private OffsetDateTime date;
    private MovementType movementType;
    private BigDecimal amount;
    private BigDecimal balance;
    private Long accountId;
    private String description;

    public static Movement createCredit(Long accountId, BigDecimal amount, BigDecimal balanceAfter, String description) {
        return Movement.builder()
                .accountId(accountId)
                .movementType(MovementType.CREDIT)
                .amount(amount)
                .balance(balanceAfter)
                .date(OffsetDateTime.now())
                .description(description)
                .build();
    }

    public static Movement createDebit(Long accountId, BigDecimal amount, BigDecimal balanceAfter, String description) {
        return Movement.builder()
                .accountId(accountId)
                .movementType(MovementType.DEBIT)
                .amount(amount)
                .balance(balanceAfter)
                .date(OffsetDateTime.now())
                .description(description)
                .build();
    }

    public boolean isCredit() {
        return MovementType.CREDIT.equals(this.movementType);
    }

    public boolean isDebit() {
        return MovementType.DEBIT.equals(this.movementType);
    }
}
