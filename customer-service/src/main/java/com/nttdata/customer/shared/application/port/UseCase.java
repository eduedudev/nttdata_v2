package com.nttdata.customer.shared.application.port;

import reactor.core.publisher.Mono;

public interface UseCase<I, O> {

    Mono<O> execute(I input);
}
