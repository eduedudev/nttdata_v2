package com.nttdata.account.infrastructure.persistence;

import com.nttdata.account.domain.Account;
import com.nttdata.account.domain.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountR2dbcRepository r2dbcRepository;
    private final AccountEntityMapper entityMapper;

    @Override
    public Mono<Account> save(Account account) {
        return r2dbcRepository.save(entityMapper.toEntity(account))
                .map(entityMapper::toDomain);
    }

    @Override
    public Mono<Account> findById(Long accountId) {
        return r2dbcRepository.findById(accountId)
                .map(entityMapper::toDomain);
    }

    @Override
    public Mono<Account> findByAccountNumber(String accountNumber) {
        return r2dbcRepository.findByAccountNumber(accountNumber)
                .map(entityMapper::toDomain);
    }

    @Override
    public Flux<Account> findAll(int page, int size) {
        return r2dbcRepository.findAllBy(PageRequest.of(page, size))
                .map(entityMapper::toDomain);
    }

    @Override
    public Flux<Account> findByCustomerId(Long customerId) {
        return r2dbcRepository.findByCustomerId(customerId)
                .map(entityMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(Long accountId) {
        return r2dbcRepository.deleteById(accountId);
    }

    @Override
    public Mono<Boolean> existsByAccountNumber(String accountNumber) {
        return r2dbcRepository.existsByAccountNumber(accountNumber);
    }

    @Override
    public Mono<Boolean> existsById(Long accountId) {
        return r2dbcRepository.existsById(accountId);
    }
}
