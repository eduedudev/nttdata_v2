package com.nttdata.account.application.create_account;

import com.nttdata.account.domain.Account;
import com.nttdata.account.domain.AccountAlreadyExistsException;
import com.nttdata.account.domain.AccountRepository;
import com.nttdata.account.domain.CustomerNotFoundException;
import com.nttdata.account.domain.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class CreateAccountCommandHandlerImpl implements CreateAccountCommandHandler {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    @Override
    public Mono<Account> handle(CreateAccountCommand command) {
        return validateCustomerExists(command.getCustomerId())
                .then(Mono.defer(() -> validateAccountNumberNotExists(command.getAccountNumber())))
                .then(Mono.defer(() -> accountRepository.save(buildAccount(command))));
    }

    private Mono<Void> validateCustomerExists(Long customerId) {
        return customerRepository.existsById(customerId)
                .flatMap(exists -> {
                    if (Boolean.FALSE.equals(exists)) {
                        return Mono.error(new CustomerNotFoundException(customerId));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> validateAccountNumberNotExists(String accountNumber) {
        return accountRepository.existsByAccountNumber(accountNumber)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new AccountAlreadyExistsException(accountNumber));
                    }
                    return Mono.empty();
                });
    }

    private Account buildAccount(CreateAccountCommand command) {
        OffsetDateTime now = OffsetDateTime.now();
        return Account.builder()
                .accountNumber(command.getAccountNumber())
                .accountType(command.getAccountType())
                .initialBalance(command.getInitialBalance())
                .currentBalance(command.getInitialBalance())
                .status(command.getStatus())
                .customerId(command.getCustomerId())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
