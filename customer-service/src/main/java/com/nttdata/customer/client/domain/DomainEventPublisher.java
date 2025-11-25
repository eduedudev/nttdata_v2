package com.nttdata.customer.client.domain;

import reactor.core.publisher.Mono;

public interface DomainEventPublisher {

    <T extends DomainEvent> Mono<Void> publish(T event);
}
