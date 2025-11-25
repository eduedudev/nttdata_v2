package com.nttdata.customer.client.infrastructure.persistence;

import com.nttdata.customer.client.domain.Customer;
import com.nttdata.customer.client.domain.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final CustomerR2dbcRepository r2dbcRepository;
    private final CustomerEntityMapper entityMapper;

    @Override
    public Mono<Customer> save(Customer customer) {
        return r2dbcRepository.save(entityMapper.toEntity(customer))
                .map(entityMapper::toDomain);
    }

    @Override
    public Mono<Customer> findById(Long id) {
        return r2dbcRepository.findById(id)
                .map(entityMapper::toDomain);
    }

    @Override
    public Mono<Customer> findByIdentification(String identification) {
        return r2dbcRepository.findByIdentification(identification)
                .map(entityMapper::toDomain);
    }

    @Override
    public Flux<Customer> findAll() {
        return r2dbcRepository.findAll()
                .map(entityMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return r2dbcRepository.deleteById(id);
    }

    @Override
    public Mono<Boolean> existsByIdentification(String identification) {
        return r2dbcRepository.existsByIdentification(identification);
    }
}
