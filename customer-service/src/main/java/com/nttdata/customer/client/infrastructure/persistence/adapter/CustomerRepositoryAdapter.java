package com.nttdata.customer.client.infrastructure.persistence.adapter;

import com.nttdata.customer.client.domain.model.Customer;
import com.nttdata.customer.client.domain.port.CustomerRepository;
import com.nttdata.customer.client.infrastructure.persistence.mapper.CustomerEntityMapper;
import com.nttdata.customer.client.infrastructure.persistence.repository.CustomerR2dbcRepository;
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
