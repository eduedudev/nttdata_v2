package com.nttdata.customer.client.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nttdata.customer.client.domain.DomainEvent;
import com.nttdata.customer.client.domain.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements DomainEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.customer-events}")
    private String customerEventsTopic;

    @Override
    public <T extends DomainEvent> Mono<Void> publish(T event) {
        return Mono.fromCallable(() -> serializeEvent(event))
                .flatMap(payload -> sendToKafka(event.getAggregateId(), payload, event.getEventType()))
                .doOnSuccess(v -> log.info("Event published: type={}, aggregateId={}", 
                        event.getEventType(), event.getAggregateId()))
                .doOnError(e -> log.error("Failed to publish event: type={}, aggregateId={}", 
                        event.getEventType(), event.getAggregateId(), e));
    }

    private String serializeEvent(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }

    private Mono<Void> sendToKafka(String key, String payload, String eventType) {
        return Mono.fromFuture(() -> kafkaTemplate.send(customerEventsTopic, key, payload).toCompletableFuture())
                .then();
    }
}
