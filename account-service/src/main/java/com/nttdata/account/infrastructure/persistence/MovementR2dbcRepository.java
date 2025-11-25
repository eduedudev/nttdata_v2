package com.nttdata.account.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Repository
public interface MovementR2dbcRepository extends ReactiveCrudRepository<MovementEntity, Long> {

    Flux<MovementEntity> findByAccountId(Long accountId, Pageable pageable);

    Mono<MovementEntity> findByMovementIdAndAccountId(Long movementId, Long accountId);

    @Query("SELECT * FROM movement WHERE account_id = :accountId AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    Flux<MovementEntity> findByAccountIdAndDateBetween(Long accountId, OffsetDateTime startDate, OffsetDateTime endDate);
}
