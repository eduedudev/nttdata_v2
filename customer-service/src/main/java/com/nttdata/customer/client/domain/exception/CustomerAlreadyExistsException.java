package com.nttdata.customer.client.domain.exception;

import com.nttdata.customer.shared.domain.exception.DomainException;

public class CustomerAlreadyExistsException extends DomainException {

    public CustomerAlreadyExistsException(String identification) {
        super("Customer with identification " + identification + " already exists");
    }
}
