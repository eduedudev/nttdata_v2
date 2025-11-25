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
        CustomerEntity entity = entityMapper.toEntity(customer);
        // If customer has ID and doesn't exist, use explicit insert
        if (customer.getCustomerId() != null) {
            return r2dbcRepository.existsById(customer.getCustomerId())
                    .flatMap(exists -> {
                        if (exists) {
                            return r2dbcRepository.save(entity).map(entityMapper::toDomain);
                        } else {
                            return r2dbcRepository.insertCustomer(entity)
                                    .then(Mono.just(customer));
                        }
                    });
        }
        return r2dbcRepository.save(entity).map(entityMapper::toDomain);
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
