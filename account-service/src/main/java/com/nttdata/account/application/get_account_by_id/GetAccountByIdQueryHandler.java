package com.nttdata.account.application.get_account_by_id;

import com.nttdata.account.domain.Account;
import reactor.core.publisher.Mono;

public interface GetAccountByIdQueryHandler {

    Mono<Account> handle(GetAccountByIdQuery query);
}
