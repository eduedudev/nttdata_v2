package com.nttdata.customer.client.domain;

import java.time.Instant;

public interface DomainEvent {

    String getEventId();

    String getEventType();

    Instant getOccurredOn();

    String getAggregateId();
}
