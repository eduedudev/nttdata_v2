package com.nttdata.customer.client.infrastructure.persistence.mapper;

import com.nttdata.customer.client.domain.model.Customer;
import com.nttdata.customer.client.domain.model.Gender;
import com.nttdata.customer.client.infrastructure.persistence.entity.CustomerEntity;
import org.springframework.stereotype.Component;

@Component
public class CustomerEntityMapper {

    public CustomerEntity toEntity(Customer customer) {
        return CustomerEntity.builder()
                .customerId(customer.getCustomerId())
                .name(customer.getName())
                .gender(customer.getGender() != null ? customer.getGender().name() : null)
                .identification(customer.getIdentification())
                .address(customer.getAddress())
                .phone(customer.getPhone())
                .password(customer.getPassword())
                .status(customer.getStatus())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    public Customer toDomain(CustomerEntity entity) {
        return Customer.builder()
                .customerId(entity.getCustomerId())
                .name(entity.getName())
                .gender(entity.getGender() != null ? Gender.valueOf(entity.getGender()) : null)
                .identification(entity.getIdentification())
                .address(entity.getAddress())
                .phone(entity.getPhone())
                .password(entity.getPassword())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
