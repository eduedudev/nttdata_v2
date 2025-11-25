package com.nttdata.customer.client.domain.exception;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long id) {
        super("Customer with id " + id + " not found");
    }

    public CustomerNotFoundException(String identification) {
        super("Customer with identification " + identification + " not found");
    }
}
