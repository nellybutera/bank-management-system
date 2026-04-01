package com.bank_management_system.exceptions;

/**
 * Thrown when a customer ID lookup in the Bank registry returns no match.
 * Distinct from {@link AccountNotFoundException} — this signals a missing customer,
 * not a missing account.
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String message) {
        super(message);
    }
}
