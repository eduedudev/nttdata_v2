package com.nttdata.account.application.delete_account;

import reactor.core.publisher.Mono;

public interface DeleteAccountCommandHandler {

    Mono<Void> handle(DeleteAccountCommand command);
}
