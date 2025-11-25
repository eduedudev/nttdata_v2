package com.nttdata.account.application.delete_account;

import com.nttdata.account.domain.AccountNotFoundException;
import com.nttdata.account.domain.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DeleteAccountCommandHandlerImpl implements DeleteAccountCommandHandler {

    private final AccountRepository accountRepository;

    @Override
    public Mono<Void> handle(DeleteAccountCommand command) {
        return accountRepository.existsById(command.getAccountId())
                .flatMap(exists -> {
                    if (Boolean.FALSE.equals(exists)) {
                        return Mono.error(new AccountNotFoundException(command.getAccountId()));
                    }
                    return accountRepository.deleteById(command.getAccountId());
                });
    }
}
