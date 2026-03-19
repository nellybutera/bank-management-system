package com.bank_management_system.exceptions;

/**
 * Thrown when a withdrawal on a CheckingAccount would push the balance
 * beyond the account's overdraft limit.
 *
 * Extends InsufficientFundsException because exceeding the overdraft limit
 * is a specific form of insufficient funds — any catch block that handles
 * InsufficientFundsException will also catch this automatically.
 */
public class OverdraftLimitExceededException extends InsufficientFundsException {

    public OverdraftLimitExceededException(String message) {
        super(message);
    }
}
