package com.nttdata.account.infrastructure.persistence;

import com.nttdata.account.domain.Movement;
import com.nttdata.account.domain.MovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class MovementRepositoryAdapter implements MovementRepository {

    private final MovementR2dbcRepository r2dbcRepository;
    private final MovementEntityMapper entityMapper;

    @Override
    public Mono<Movement> save(Movement movement) {
        return r2dbcRepository.save(entityMapper.toEntity(movement))
                .map(entityMapper::toDomain);
    }

    @Override
    public Mono<Movement> findById(Long movementId) {
        return r2dbcRepository.findById(movementId)
                .map(entityMapper::toDomain);
    }

    @Override
    public Mono<Movement> findByIdAndAccountId(Long movementId, Long accountId) {
        return r2dbcRepository.findByMovementIdAndAccountId(movementId, accountId)
                .map(entityMapper::toDomain);
    }

    @Override
    public Flux<Movement> findByAccountId(Long accountId, int page, int size) {
        return r2dbcRepository.findByAccountId(accountId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date")))
                .map(entityMapper::toDomain);
    }

    @Override
    public Flux<Movement> findByAccountIdAndDateBetween(Long accountId, OffsetDateTime startDate, OffsetDateTime endDate) {
        return r2dbcRepository.findByAccountIdAndDateBetween(accountId, startDate, endDate)
                .map(entityMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(Long movementId) {
        return r2dbcRepository.deleteById(movementId);
    }

    @Override
    public Mono<Boolean> existsById(Long movementId) {
        return r2dbcRepository.existsById(movementId);
    }
}
