package com.nttdata.customer.client.application.mapper;

import com.nttdata.customer.api.model.CustomerRequest;
import com.nttdata.customer.api.model.CustomerResponse;
import com.nttdata.customer.client.domain.model.Customer;
import com.nttdata.customer.client.domain.model.Gender;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toDomain(CustomerRequest request) {
        return Customer.builder()
                .name(request.getNombre())
                .gender(mapGender(request.getGenero()))
                .identification(request.getIdentificacion())
                .address(request.getDireccion())
                .phone(request.getTelefono())
                .password(request.getContrasena())
                .status(request.getEstado())
                .build();
    }

    public CustomerResponse toResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setClienteId(customer.getCustomerId());
        response.setNombre(customer.getName());
        response.setGenero(mapGeneroResponse(customer.getGender()));
        response.setIdentificacion(customer.getIdentification());
        response.setDireccion(customer.getAddress());
        response.setTelefono(customer.getPhone());
        response.setEstado(customer.getStatus());
        response.setFechaCreacion(customer.getCreatedAt());
        response.setFechaActualizacion(customer.getUpdatedAt());
        return response;
    }

    private Gender mapGender(CustomerRequest.GeneroEnum genero) {
        if (genero == null) {
            return null;
        }
        return switch (genero) {
            case MALE -> Gender.MALE;
            case FEMALE -> Gender.FEMALE;
            case OTHER -> Gender.OTHER;
        };
    }

    private CustomerResponse.GeneroEnum mapGeneroResponse(Gender gender) {
        if (gender == null) {
            return null;
        }
        return switch (gender) {
            case MALE -> CustomerResponse.GeneroEnum.MALE;
            case FEMALE -> CustomerResponse.GeneroEnum.FEMALE;
            case OTHER -> CustomerResponse.GeneroEnum.OTHER;
        };
    }
}
