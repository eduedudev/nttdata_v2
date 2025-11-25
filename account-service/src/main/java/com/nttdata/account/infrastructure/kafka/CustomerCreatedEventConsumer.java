package com.nttdata.account.infrastructure.kafka;

import com.nttdata.account.application.register_customer.RegisterCustomerCommand;
import com.nttdata.account.application.register_customer.RegisterCustomerCommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerCreatedEventConsumer {

    private final RegisterCustomerCommandHandler registerCustomerCommandHandler;

    @KafkaListener(topics = "${kafka.topics.customer-created:customer-created}", 
                   groupId = "${spring.kafka.consumer.group-id:account-service-group}",
                   containerFactory = "kafkaListenerContainerFactory")
    public void consumeCustomerCreatedEvent(CustomerCreatedEvent event) {
        log.info("Received customer created event: customerId={}, name={}", 
                event.getCustomerId(), event.getName());

        RegisterCustomerCommand command = RegisterCustomerCommand.builder()
                .customerId(event.getCustomerId())
                .name(event.getName())
                .identification(event.getIdentification())
                .address(event.getAddress())
                .phone(event.getPhone())
                .build();

        registerCustomerCommandHandler.handle(command)
                .doOnSuccess(customer -> log.info("Customer registered successfully: customerId={}", 
                        customer.getCustomerId()))
                .doOnError(error -> log.error("Error registering customer: customerId={}, error={}", 
                        event.getCustomerId(), error.getMessage()))
                .onErrorResume(error -> {
                    // Log error but don't fail the consumer
                    log.error("Failed to process customer created event", error);
                    return Mono.empty();
                })
                .subscribe();
    }
}
