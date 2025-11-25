package com.nttdata.customer.client.application.get_customer_by_id;

import com.nttdata.customer.client.domain.Customer;
import com.nttdata.customer.client.domain.CustomerNotFoundException;
import com.nttdata.customer.client.domain.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetCustomerByIdQueryHandlerImpl implements GetCustomerByIdQueryHandler {

    private final CustomerRepository customerRepository;

    @Override
    public Mono<Customer> handle(GetCustomerByIdQuery query) {
        return customerRepository.findById(query.getCustomerId())
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(query.getCustomerId())));
    }
}
