package com.nttdata.customer.client.application.delete_customer;

import com.nttdata.customer.client.domain.CustomerNotFoundException;
import com.nttdata.customer.client.domain.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DeleteCustomerCommandHandlerImpl implements DeleteCustomerCommandHandler {

    private final CustomerRepository customerRepository;

    @Override
    public Mono<Void> handle(DeleteCustomerCommand command) {
        return customerRepository.findById(command.getCustomerId())
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(command.getCustomerId())))
                .flatMap(customer -> customerRepository.deleteById(customer.getCustomerId()));
    }
}
