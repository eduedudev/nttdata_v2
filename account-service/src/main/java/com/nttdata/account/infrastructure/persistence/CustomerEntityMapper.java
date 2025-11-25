package com.nttdata.account.infrastructure.persistence;

import com.nttdata.account.domain.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerEntityMapper {

    public CustomerEntity toEntity(Customer customer) {
        return CustomerEntity.builder()
                .customerId(customer.getCustomerId())
                .name(customer.getName())
                .identification(customer.getIdentification())
                .address(customer.getAddress())
                .phone(customer.getPhone())
                .status(customer.getStatus())
                .build();
    }

    public Customer toDomain(CustomerEntity entity) {
        return Customer.builder()
                .customerId(entity.getCustomerId())
                .name(entity.getName())
                .identification(entity.getIdentification())
                .address(entity.getAddress())
                .phone(entity.getPhone())
                .status(entity.getStatus())
                .build();
    }
}
