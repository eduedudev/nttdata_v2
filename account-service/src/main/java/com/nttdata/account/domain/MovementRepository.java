package com.nttdata.account.domain;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public interface MovementRepository {

    Mono<Movement> save(Movement movement);

    Mono<Movement> findById(Long movementId);

    Mono<Movement> findByIdAndAccountId(Long movementId, Long accountId);

    Flux<Movement> findByAccountId(Long accountId, int page, int size);

    Flux<Movement> findByAccountIdAndDateBetween(Long accountId, OffsetDateTime startDate, OffsetDateTime endDate);

    Mono<Void> deleteById(Long movementId);

    Mono<Boolean> existsById(Long movementId);
}
