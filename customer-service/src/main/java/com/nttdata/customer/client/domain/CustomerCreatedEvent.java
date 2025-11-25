package com.nttdata.customer.client.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class CustomerCreatedEvent implements DomainEvent {

    @Builder.Default
    private final String eventId = UUID.randomUUID().toString();

    @Builder.Default
    private final String eventType = "CustomerCreated";

    @Builder.Default
    private final Instant occurredOn = Instant.now();

    private final String aggregateId;
    private final String name;
    private final String identification;
    private final String gender;
    private final String address;
    private final String phone;
    private final Boolean status;

    public static CustomerCreatedEvent fromCustomer(Customer customer) {
        return CustomerCreatedEvent.builder()
                .aggregateId(String.valueOf(customer.getCustomerId()))
                .name(customer.getName())
                .identification(customer.getIdentification())
                .gender(customer.getGender().name())
                .address(customer.getAddress())
                .phone(customer.getPhone())
                .status(customer.getStatus())
                .build();
    }
}
