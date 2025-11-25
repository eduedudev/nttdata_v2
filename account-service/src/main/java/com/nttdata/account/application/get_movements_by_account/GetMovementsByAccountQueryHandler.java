package com.nttdata.account.application.get_movements_by_account;

import com.nttdata.account.domain.Movement;
import reactor.core.publisher.Flux;

public interface GetMovementsByAccountQueryHandler {

    Flux<Movement> handle(GetMovementsByAccountQuery query);
}
