package com.bank_management_system.exceptions;

/**
 * Thrown when an operation is invalid given the current state of an object —
 * e.g. depositing to a closed account, or closing an account that still holds a balance.
 * Domain-specific alternative to {@link java.lang.IllegalStateException}.
 */
public class IllegalStateException extends RuntimeException {
    public IllegalStateException(String message){
        super(message);
    }
}
