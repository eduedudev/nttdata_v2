package com.nttdata.account.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AccountR2dbcRepository extends ReactiveCrudRepository<AccountEntity, Long> {

    Mono<AccountEntity> findByAccountNumber(String accountNumber);

    Mono<Boolean> existsByAccountNumber(String accountNumber);

    Flux<AccountEntity> findAllBy(Pageable pageable);

    Flux<AccountEntity> findByCustomerId(Long customerId);
}
