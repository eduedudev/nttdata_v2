package com.nttdata.account.application.create_account;

import com.nttdata.account.domain.Account;
import reactor.core.publisher.Mono;

public interface CreateAccountCommandHandler {

    Mono<Account> handle(CreateAccountCommand command);
}
