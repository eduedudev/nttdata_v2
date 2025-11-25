package com.nttdata.account.application.update_account;

import com.nttdata.account.domain.Account;
import com.nttdata.account.domain.AccountAlreadyExistsException;
import com.nttdata.account.domain.AccountNotFoundException;
import com.nttdata.account.domain.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UpdateAccountCommandHandlerImpl implements UpdateAccountCommandHandler {

    private final AccountRepository accountRepository;

    @Override
    public Mono<Account> handle(UpdateAccountCommand command) {
        return accountRepository.findById(command.getAccountId())
                .switchIfEmpty(Mono.error(new AccountNotFoundException(command.getAccountId())))
                .flatMap(existingAccount -> validateAccountNumberNotTaken(command, existingAccount))
                .map(existingAccount -> existingAccount.update(
                        command.getAccountNumber(),
                        command.getAccountType(),
                        command.getInitialBalance(),
                        command.getStatus()))
                .flatMap(accountRepository::save);
    }

    private Mono<Account> validateAccountNumberNotTaken(UpdateAccountCommand command, Account existingAccount) {
        if (existingAccount.getAccountNumber().equals(command.getAccountNumber())) {
            return Mono.just(existingAccount);
        }
        return accountRepository.existsByAccountNumber(command.getAccountNumber())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new AccountAlreadyExistsException(command.getAccountNumber()));
                    }
                    return Mono.just(existingAccount);
                });
    }
}
