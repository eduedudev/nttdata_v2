package com.nttdata.account.application.register_movement;

import com.nttdata.account.domain.MovementType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RegisterMovementCommand {

    private final Long accountId;
    private final MovementType movementType;
    private final BigDecimal amount;
    private final String description;
}
