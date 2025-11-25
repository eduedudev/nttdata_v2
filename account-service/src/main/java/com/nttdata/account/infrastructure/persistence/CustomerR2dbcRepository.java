package com.nttdata.account.infrastructure.persistence;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerR2dbcRepository extends ReactiveCrudRepository<CustomerEntity, Long> {

    Mono<CustomerEntity> findByIdentification(String identification);

    @Modifying
    @Query("INSERT INTO customer (customer_id, name, identification, address, phone, status) VALUES (:#{#entity.customerId}, :#{#entity.name}, :#{#entity.identification}, :#{#entity.address}, :#{#entity.phone}, :#{#entity.status})")
    Mono<Void> insertCustomer(CustomerEntity entity);
}
