package com.bank_management_system.exceptions;

public class InputValidator {

    private InputValidator() {
        // private constructor to prevent instantiation
    }

    /**
     * Validates that a name is non-blank and contains only letters, spaces, hyphens, or apostrophes.
     *
     * @param name the name to validate
     * @throws IllegalArgumentException if the name is blank or contains invalid characters
     */
    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        if (!name.trim().matches("[a-zA-Z][a-zA-Z\\s\\-']*")) {
            throw new IllegalArgumentException("Name must contain only letters, spaces, hyphens, or apostrophes");
        }
    }

    /**
     * Validates that an amount is greater than zero.
     *
     * @param amount the amount to validate
     * @throws InvalidAmountException if the amount is zero or negative
     */
    public static void validateAmount(double amount) {
        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero. Received: " + amount);
        }
    }

    /**
     * Validates that a menu choice falls within the allowed range.
     *
     * @param choice the user's selection
     * @param min    the minimum valid option
     * @param max    the maximum valid option
     * @throws IllegalArgumentException if the choice is outside [min, max]
     */
    public static void validateMenuChoice(int choice, int min, int max) {
        if (choice < min || choice > max) {
            throw new IllegalArgumentException(
                    "Invalid choice. Please enter a number between " + min + " and " + max + ".");
        }
    }

    /**
     * Validates that a generic ID field is non-blank.
     *
     * @param id    the ID value to check
     * @param label a human-readable label used in the error message (e.g. "Account number")
     * @throws IllegalArgumentException if the ID is null or blank
     */
    public static void validateId(String id, String label) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " must not be blank.");
        }
    }

    /**
     * Validates that an age is within the acceptable range of 18 to 120.
     *
     * @param age the age to validate
     * @throws IllegalArgumentException if the age is outside the valid range
     */
    public static void validateAge(int age) {
        if (age < 18 || age > 120) {
            throw new IllegalArgumentException("Age must be between 18 and 120. Provided: " + age);
        }
    }

    /**
     * Validates that a contact number is non-blank and matches a basic phone format.
     * Accepts 7–15 characters consisting of digits, spaces, hyphens, dots, or parentheses,
     * with an optional leading +.
     *
     * @param contact the contact number to validate
     * @throws IllegalArgumentException if the contact is blank or does not match the phone format
     */
    public static void validateContact(String contact) {
        if (contact == null || contact.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact must not be blank");
        }
        if (!contact.trim().matches("[+]?[\\d][\\d\\s\\-().]{6,14}")) {
            throw new IllegalArgumentException(
                    "Contact must be a valid phone number (7–15 digits; may include +, -, spaces, or parentheses)");
        }
    }

    /**
     * Validates that an address is non-blank.
     * Allows letters, digits, spaces, commas, periods, hyphens, and apostrophes
     * to accommodate formats like "123 Main St, Springfield".
     *
     * @param address the address to validate
     * @throws IllegalArgumentException if the address is blank or contains invalid characters
     */
    public static void validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address must not be blank");
        }
        if (!address.trim().matches("[a-zA-Z0-9][a-zA-Z0-9\\s,.'\\-]*")) {
            throw new IllegalArgumentException(
                    "Address must contain only letters, numbers, spaces, commas, or hyphens");
        }
    }

    /**
     * Validates that an account number matches the expected format (e.g. ACC001).
     *
     * @param accountNumber the account number to validate
     * @throws IllegalArgumentException if the account number is blank or incorrectly formatted
     */
    public static void validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number must not be blank");
        }
        if (!accountNumber.trim().matches("(?i)ACC\\d+")) {
            throw new IllegalArgumentException("Invalid account number format. Expected format: ACC001");
        }
    }

    /**
     * Validates that a customer ID matches the expected format (e.g. CUST001).
     *
     * @param customerId the customer ID to validate
     * @throws IllegalArgumentException if the customer ID is blank or incorrectly formatted
     */
    public static void validateCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID must not be blank");
        }
        if (!customerId.trim().matches("(?i)CUST\\d+")) {
            throw new IllegalArgumentException("Invalid customer ID format. Expected format: CUST001");
        }
    }
}
