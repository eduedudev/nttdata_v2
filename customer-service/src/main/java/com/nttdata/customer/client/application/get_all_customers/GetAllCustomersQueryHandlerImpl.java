package com.nttdata.customer.client.application.get_all_customers;

import com.nttdata.customer.client.domain.Customer;
import com.nttdata.customer.client.domain.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class GetAllCustomersQueryHandlerImpl implements GetAllCustomersQueryHandler {

    private final CustomerRepository customerRepository;

    @Override
    public Flux<Customer> handle(GetAllCustomersQuery query) {
        return customerRepository.findAll()
                .skip((long) query.getPageOrDefault() * query.getSizeOrDefault())
                .take(query.getSizeOrDefault());
    }
}
