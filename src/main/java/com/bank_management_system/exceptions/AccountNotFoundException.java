package com.bank_management_system.exceptions;

/** Thrown when an account number lookup in {@link com.bank_management_system.accounts.AccountManager} returns no match. */
public class AccountNotFoundException extends RuntimeException{
    public AccountNotFoundException(String message){
        super(message);
    }
}
