package com.nttdata.account.application.register_movement;

import com.nttdata.account.domain.Movement;
import reactor.core.publisher.Mono;

public interface RegisterMovementCommandHandler {

    Mono<Movement> handle(RegisterMovementCommand command);
}
