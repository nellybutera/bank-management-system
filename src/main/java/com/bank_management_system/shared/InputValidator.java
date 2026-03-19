package com.bank_management_system.shared;

public class InputValidator {
    private InputValidator() {
        // private constructor to prevent instantiation
    }

    public static void validateName(String name){
        if (name == null || name.trim().isEmpty()){
            throw new IllegalArgumentException("Name must not be blank");
        }
        if (!name.trim().matches("[a-zA-Z][a-zA-Z\\s\\-']*")){
            throw new IllegalArgumentException("Name must contain only letters, spaces, hyphens, or apostrophes");
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

    public static void validateAge(int age) {
        if (age < 18 || age > 120) {
            throw new IllegalArgumentException("Age must be between 18 and 120. Provided: " + age);
        }
    }

    public static void validateContact(String contact) {
        if (contact == null || contact.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact must not be blank");
        }
        // Allows optional leading +, then 7–15 characters of digits, spaces, dashes, dots, or parentheses
        if (!contact.trim().matches("[+]?[\\d][\\d\\s\\-().]{6,14}")) {
            throw new IllegalArgumentException(
                    "Contact must be a valid phone number (7–15 digits; may include +, -, spaces, or parentheses)");
        }
    }

    public static void validateAddress(String address){
        if (address == null || address.trim().isEmpty()){
            throw new IllegalArgumentException("Address must not be blank");
        }
        if (!address.trim().matches("[a-zA-Z][a-zA-Z\\s\\-']*")){
            throw new IllegalArgumentException("Address must contain only letters, spaces, hyphens, or apostrophes");
        }
    }


    public static void validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number must not be blank");
        }
        if (!accountNumber.trim().matches("(?i)ACC\\d+")) {
            throw new IllegalArgumentException(
                    "Invalid account number format. Expected format: ACC001");
        }
    }

    public static void validateCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID must not be blank");
        }
        if (!customerId.trim().matches("(?i)CUST\\d+")) {
            throw new IllegalArgumentException(
                    "Invalid customer ID format. Expected format: CUST001");
        }
    }
}
