package com.bank_management_system.exceptions;

/**
 * Thrown when a transaction amount is zero, negative, or otherwise invalid
 * (e.g. an initial deposit below the savings account minimum balance).
 */
public class InvalidAmountException extends RuntimeException {

    public InvalidAmountException(String message) {
        super(message);
    }
}