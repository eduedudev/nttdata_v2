package com.nttdata.customer.client.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nttdata.customer.client.domain.CustomerCreatedEvent;
import com.nttdata.customer.client.domain.CustomerCreatedEventMother;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaEventPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ObjectMapper objectMapper;
    private KafkaEventPublisher kafkaEventPublisher;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        kafkaEventPublisher = new KafkaEventPublisher(kafkaTemplate, objectMapper);
        ReflectionTestUtils.setField(kafkaEventPublisher, "customerEventsTopic", "customer-events");
    }

    @Test
    void shouldPublishCustomerCreatedEvent() {
        CustomerCreatedEvent event = CustomerCreatedEventMother.withCustomerId(1L);
        SendResult<String, String> sendResult = createSendResult();
        when(kafkaTemplate.send(eq("customer-events"), eq("1"), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        StepVerifier.create(kafkaEventPublisher.publish(event))
                .verifyComplete();

        verify(kafkaTemplate).send(eq("customer-events"), eq("1"), anyString());
    }

    @Test
    void shouldSerializeEventAsJson() {
        CustomerCreatedEvent event = CustomerCreatedEventMother.withCustomerId(1L);
        SendResult<String, String> sendResult = createSendResult();
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(eq("customer-events"), eq("1"), payloadCaptor.capture()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        StepVerifier.create(kafkaEventPublisher.publish(event))
                .verifyComplete();

        String payload = payloadCaptor.getValue();
        assertTrue(payload.contains("\"eventType\":\"CustomerCreated\""));
        assertTrue(payload.contains("\"aggregateId\":\"1\""));
        assertTrue(payload.contains("\"name\":\"John Doe\""));
    }

    @Test
    void shouldUseAggregateIdAsKafkaKey() {
        CustomerCreatedEvent event = CustomerCreatedEventMother.withCustomerId(99L);
        SendResult<String, String> sendResult = createSendResult();
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(eq("customer-events"), keyCaptor.capture(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        StepVerifier.create(kafkaEventPublisher.publish(event))
                .verifyComplete();

        assertEquals("99", keyCaptor.getValue());
    }

    @Test
    void shouldHandleKafkaError() {
        CustomerCreatedEvent event = CustomerCreatedEventMother.withCustomerId(1L);
        CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka connection failed"));
        when(kafkaTemplate.send(eq("customer-events"), eq("1"), anyString()))
                .thenReturn(failedFuture);

        StepVerifier.create(kafkaEventPublisher.publish(event))
                .expectError(RuntimeException.class)
                .verify();
    }

    private SendResult<String, String> createSendResult() {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("customer-events", "1", "{}");
        RecordMetadata recordMetadata = new RecordMetadata(
                new TopicPartition("customer-events", 0), 0L, 0, 0L, 0, 0);
        return new SendResult<>(producerRecord, recordMetadata);
    }
}
