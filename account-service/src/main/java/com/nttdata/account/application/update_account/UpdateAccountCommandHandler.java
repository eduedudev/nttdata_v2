package com.nttdata.account.application.update_account;

import com.nttdata.account.domain.Account;
import reactor.core.publisher.Mono;

public interface UpdateAccountCommandHandler {

    Mono<Account> handle(UpdateAccountCommand command);
}
