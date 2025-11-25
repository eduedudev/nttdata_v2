package com.nttdata.customer.client.application;

import com.nttdata.customer.api.model.CustomerRequest;

public class CustomerRequestMother {

    public static CustomerRequest createDefault() {
        CustomerRequest request = new CustomerRequest();
        request.setName("John Doe");
        request.setGender(CustomerRequest.GenderEnum.MALE);
        request.setIdentification("1234567890");
        request.setAddress("123 Main Street");
        request.setPhone("+573001234567");
        request.setPassword("password123");
        request.setStatus(true);
        return request;
    }

    public static CustomerRequest createFemale() {
        CustomerRequest request = createDefault();
        request.setName("Jane Doe");
        request.setGender(CustomerRequest.GenderEnum.FEMALE);
        request.setIdentification("0987654321");
        return request;
    }

    public static CustomerRequest createOtherGender() {
        CustomerRequest request = createDefault();
        request.setName("Alex Smith");
        request.setGender(CustomerRequest.GenderEnum.OTHER);
        request.setIdentification("1111111111");
        return request;
    }

    public static CustomerRequest createWithNullGender() {
        CustomerRequest request = createDefault();
        request.setGender(null);
        return request;
    }

    public static CustomerRequest createInactive() {
        CustomerRequest request = createDefault();
        request.setStatus(false);
        return request;
    }

    public static CustomerRequest createWithIdentification(String identification) {
        CustomerRequest request = createDefault();
        request.setIdentification(identification);
        return request;
    }
}
