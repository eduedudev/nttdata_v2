package com.nttdata.account.infrastructure.persistence;

import com.nttdata.account.domain.Customer;
import com.nttdata.account.domain.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
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
    public Mono<Customer> findById(Long customerId) {
        return r2dbcRepository.findById(customerId)
                .map(entityMapper::toDomain);
    }

    @Override
    public Mono<Customer> findByIdentification(String identification) {
        return r2dbcRepository.findByIdentification(identification)
                .map(entityMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(Long customerId) {
        return r2dbcRepository.deleteById(customerId);
    }

    @Override
    public Mono<Boolean> existsById(Long customerId) {
        return r2dbcRepository.existsById(customerId);
    }
}
