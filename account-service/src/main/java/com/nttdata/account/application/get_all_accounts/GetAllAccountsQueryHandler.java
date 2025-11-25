package com.nttdata.account.application.get_all_accounts;

import com.nttdata.account.domain.Account;
import reactor.core.publisher.Flux;

public interface GetAllAccountsQueryHandler {

    Flux<Account> handle(GetAllAccountsQuery query);
}
