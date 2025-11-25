package com.nttdata.account.infrastructure.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerCreatedEvent {

    private String eventId;
    private String eventType;
    private String occurredOn;
    private String aggregateId;  // customerId as String
    private String name;
    private String identification;
    private String gender;
    private String address;
    private String phone;
    private Boolean status;

    public Long getCustomerId() {
        return aggregateId != null ? Long.parseLong(aggregateId) : null;
    }
}
