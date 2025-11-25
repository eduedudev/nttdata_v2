package com.nttdata.account.infrastructure.persistence;

import com.nttdata.account.domain.Movement;
import com.nttdata.account.domain.MovementType;
import org.springframework.stereotype.Component;

@Component
public class MovementEntityMapper {

    public MovementEntity toEntity(Movement movement) {
        return MovementEntity.builder()
                .movementId(movement.getMovementId())
                .date(movement.getDate())
                .movementType(movement.getMovementType() != null ? movement.getMovementType().name() : null)
                .amount(movement.getAmount())
                .balance(movement.getBalance())
                .accountId(movement.getAccountId())
                .description(movement.getDescription())
                .build();
    }

    public Movement toDomain(MovementEntity entity) {
        return Movement.builder()
                .movementId(entity.getMovementId())
                .date(entity.getDate())
                .movementType(entity.getMovementType() != null ? MovementType.valueOf(entity.getMovementType()) : null)
                .amount(entity.getAmount())
                .balance(entity.getBalance())
                .accountId(entity.getAccountId())
                .description(entity.getDescription())
                .build();
    }
}
