package com.nttdata.customer.client.application;

import com.nttdata.customer.api.model.CustomerRequest;
import com.nttdata.customer.api.model.CustomerResponse;
import com.nttdata.customer.client.application.create_customer.CreateCustomerCommand;
import com.nttdata.customer.client.application.update_customer.UpdateCustomerCommand;
import com.nttdata.customer.client.domain.Customer;
import com.nttdata.customer.client.domain.Gender;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CreateCustomerCommand toCommand(CustomerRequest request) {
        return CreateCustomerCommand.builder()
                .name(request.getName())
                .gender(mapGender(request.getGender()))
                .identification(request.getIdentification())
                .address(request.getAddress())
                .phone(request.getPhone())
                .password(request.getPassword())
                .status(request.getStatus())
                .build();
    }

    public UpdateCustomerCommand toUpdateCommand(Long customerId, CustomerRequest request) {
        return UpdateCustomerCommand.builder()
                .customerId(customerId)
                .name(request.getName())
                .gender(mapGender(request.getGender()))
                .address(request.getAddress())
                .phone(request.getPhone())
                .password(request.getPassword())
                .status(request.getStatus())
                .build();
    }

    public CustomerResponse toResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setCustomerId(customer.getCustomerId());
        response.setName(customer.getName());
        response.setGender(mapGenderResponse(customer.getGender()));
        response.setIdentification(customer.getIdentification());
        response.setAddress(customer.getAddress());
        response.setPhone(customer.getPhone());
        response.setStatus(customer.getStatus());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());
        return response;
    }

    private Gender mapGender(CustomerRequest.GenderEnum gender) {
        if (gender == null) {
            return null;
        }
        return switch (gender) {
            case MALE -> Gender.MALE;
            case FEMALE -> Gender.FEMALE;
            case OTHER -> Gender.OTHER;
        };
    }

    private CustomerResponse.GenderEnum mapGenderResponse(Gender gender) {
        if (gender == null) {
            return null;
        }
        return switch (gender) {
            case MALE -> CustomerResponse.GenderEnum.MALE;
            case FEMALE -> CustomerResponse.GenderEnum.FEMALE;
            case OTHER -> CustomerResponse.GenderEnum.OTHER;
        };
    }
}
