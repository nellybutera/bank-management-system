package com.bank_management_system.shared;

public class InputValidator {
    private InputValidator() {
        // private constructor to prevent instantiation
    }

    public static void validateName(String name){
        if (name == null || name.trim().isEmpty()){
            throw new IllegalArgumentException("Name must not be blank");
        }
    }

    public static void validateAmount(double amount){
        if (amount <= 0){
            throw new InvalidAmountException("Amount must be greater than zero. Received: "+ amount );
        }
    }

    public static void validateMenuChoice(int choice, int min, int max){
        if (choice < min || choice > max) { // Outside the valid range of menu options
            throw new IllegalArgumentException(
                    "Invalid choice. Please enter a number between " + min + " and " + max + "."); // Tells the user the exact valid range
        }
    }

    public static void validateId(String id, String label) throws IllegalArgumentException {
        if (id == null || id.trim().isEmpty()) { // null check first, then whitespace-only check
            throw new IllegalArgumentException(label + " must not be blank."); // Uses the label to make the message specific, e.g. "Account number must not be blank."
        }
        // If this line is reached, the ID is non-empty and control returns to the caller
    }
}
