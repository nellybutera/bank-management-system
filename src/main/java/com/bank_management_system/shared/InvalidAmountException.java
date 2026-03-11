package com.bank_management_system.shared;

public class InvalidAmountException extends RuntimeException{ // this makes this class a checked exception that must be handled by the caller
    
    public InvalidAmountException(String message){ // constructor accepts an error message
        super(message); // passes the error message to parent Exception class so it can be accessed using the getMessage function
    }
}