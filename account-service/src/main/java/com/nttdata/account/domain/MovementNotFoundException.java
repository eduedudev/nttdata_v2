package com.nttdata.account.domain;

public class MovementNotFoundException extends RuntimeException {

    private final Long movementId;

    public MovementNotFoundException(Long movementId) {
        super(String.format("Movement not found with id: %d", movementId));
        this.movementId = movementId;
    }

    public Long getMovementId() {
        return movementId;
    }
}
