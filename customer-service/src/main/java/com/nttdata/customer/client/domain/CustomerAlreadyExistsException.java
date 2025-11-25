package com.nttdata.customer.client.domain;

import com.nttdata.customer.shared.domain.DomainException;

public class CustomerAlreadyExistsException extends DomainException {

    public CustomerAlreadyExistsException(String identification) {
        super("Customer with identification " + identification + " already exists");
    }
}
