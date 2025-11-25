package com.nttdata.account.application.register_movement;

import com.nttdata.account.domain.MovementType;

import java.math.BigDecimal;

public class RegisterMovementCommandMother {

    public static RegisterMovementCommand.RegisterMovementCommandBuilder validCommand() {
        return RegisterMovementCommand.builder()
                .accountId(1L)
                .movementType(MovementType.CREDIT)
                .amount(BigDecimal.valueOf(500))
                .description("Test deposit");
    }

    public static RegisterMovementCommand createDefault() {
        return validCommand().build();
    }

    public static RegisterMovementCommand createCredit(BigDecimal amount) {
        return validCommand()
                .movementType(MovementType.CREDIT)
                .amount(amount)
                .description("Credit movement")
                .build();
    }

    public static RegisterMovementCommand createDebit(BigDecimal amount) {
        return validCommand()
                .movementType(MovementType.DEBIT)
                .amount(amount)
                .description("Debit movement")
                .build();
    }

    public static RegisterMovementCommand createWithAccountId(Long accountId) {
        return validCommand().accountId(accountId).build();
    }

    public static RegisterMovementCommand createWithAmount(BigDecimal amount) {
        return validCommand().amount(amount).build();
    }

    public static RegisterMovementCommand createWithDescription(String description) {
        return validCommand().description(description).build();
    }

    public static RegisterMovementCommand createWithZeroAmount() {
        return validCommand().amount(BigDecimal.ZERO).build();
    }

    public static RegisterMovementCommand createWithNegativeAmount() {
        return validCommand().amount(BigDecimal.valueOf(-100)).build();
    }
}
