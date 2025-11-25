package com.nttdata.account.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerR2dbcRepository extends ReactiveCrudRepository<CustomerEntity, Long> {

    Mono<CustomerEntity> findByIdentification(String identification);
}
