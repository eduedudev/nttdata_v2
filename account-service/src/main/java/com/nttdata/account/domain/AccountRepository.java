package com.nttdata.account.domain;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountRepository {

    Mono<Account> save(Account account);

    Mono<Account> findById(Long accountId);

    Mono<Account> findByAccountNumber(String accountNumber);

    Flux<Account> findAll(int page, int size);

    Flux<Account> findByCustomerId(Long customerId);

    Mono<Void> deleteById(Long accountId);

    Mono<Boolean> existsByAccountNumber(String accountNumber);

    Mono<Boolean> existsById(Long accountId);
}
