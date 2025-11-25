package com.nttdata.customer.client.domain;

import com.nttdata.customer.shared.domain.DomainException;

public class CustomerNotFoundException extends DomainException {

    public CustomerNotFoundException(Long id) {
        super("Customer with id " + id + " not found");
    }

    public CustomerNotFoundException(String identification) {
        super("Customer with identification " + identification + " not found");
    }
}
