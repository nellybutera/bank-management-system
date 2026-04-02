package com.bank_management_system.exceptions;

/**
 * Thrown when a method receives an argument that violates its contract —
 * e.g. an unrecognised transaction type or a same-account transfer.
 * Domain-specific alternative to {@link java.lang.IllegalArgumentException}.
 */
public class IllegalArgumentException extends RuntimeException{

    public IllegalArgumentException(String message){
        super(message);
    }
    
}