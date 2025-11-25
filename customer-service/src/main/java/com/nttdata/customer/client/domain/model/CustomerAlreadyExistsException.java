package com.nttdata.customer.client.domain.exception;

public class CustomerAlreadyExistsException extends RuntimeException {

    public CustomerAlreadyExistsException(String identification) {
        super("Customer with identification " + identification + " already exists");
    }
}
