package com.nttdata.account.application.get_all_accounts;

import com.nttdata.account.domain.Account;
import com.nttdata.account.domain.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class GetAllAccountsQueryHandlerImpl implements GetAllAccountsQueryHandler {

    private final AccountRepository accountRepository;

    @Override
    public Flux<Account> handle(GetAllAccountsQuery query) {
        return accountRepository.findAll(query.getPage(), query.getSize());
    }
}
