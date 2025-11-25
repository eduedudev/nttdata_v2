package com.nttdata.customer.client.domain.exception;

import com.nttdata.customer.shared.domain.exception.DomainException;

public class CustomerNotFoundException extends DomainException {

    public CustomerNotFoundException(Long id) {
        super("Customer with id " + id + " not found");
    }

    public CustomerNotFoundException(String identification) {
        super("Customer with identification " + identification + " not found");
    }
}
