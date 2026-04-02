package com.bank_management_system.exceptions;

/**
 * Thrown when a withdrawal cannot be completed because the account lacks sufficient funds.
 * {@link OverdraftLimitExceededException} extends this class for checking-account overdraft violations,
 * so a single {@code catch (InsufficientFundsException e)} handles both cases.
 */
public class InsufficientFundsException extends RuntimeException{

    public InsufficientFundsException(String message){
        super(message);
    }
    
}
