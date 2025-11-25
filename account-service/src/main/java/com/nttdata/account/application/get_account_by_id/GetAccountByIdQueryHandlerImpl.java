package com.nttdata.account.application.get_account_by_id;

import com.nttdata.account.domain.Account;
import com.nttdata.account.domain.AccountNotFoundException;
import com.nttdata.account.domain.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetAccountByIdQueryHandlerImpl implements GetAccountByIdQueryHandler {

    private final AccountRepository accountRepository;

    @Override
    public Mono<Account> handle(GetAccountByIdQuery query) {
        return accountRepository.findById(query.getAccountId())
                .switchIfEmpty(Mono.error(new AccountNotFoundException(query.getAccountId())));
    }
}
