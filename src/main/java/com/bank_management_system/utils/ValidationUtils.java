package com.bank_management_system.utils;

import com.bank_management_system.exceptions.IllegalArgumentException;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern ACCOUNT_NUMBER = Pattern.compile("(?i)ACC\\d{3}");
    private static final Pattern CUSTOMER_ID    = Pattern.compile("(?i)CUST\\d+");
    private static final Pattern EMAIL          = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE          = Pattern.compile("[+]?[\\d][\\d\\s\\-().]{6,14}");
    private static final Pattern NAME           = Pattern.compile("[a-zA-Z][a-zA-Z\\s\\-']*");

    // Predicate constants for dynamic / functional validation use
    public static final Predicate<String> isValidEmail         = input -> test(EMAIL, input);
    public static final Predicate<String> isValidAccountNumber = input -> test(ACCOUNT_NUMBER, input);
    public static final Predicate<String> isValidCustomerId    = input -> test(CUSTOMER_ID, input);
    public static final Predicate<String> isValidPhone         = input -> test(PHONE, input);
    public static final Predicate<String> isValidName          = input -> test(NAME, input);

    private ValidationUtils() {}

    // ── core ─────────────────────────────────────────────────────────────────

    /** Matches input against the given pattern using an explicit Matcher. */
    private static boolean test(Pattern pattern, String input) {
        if (input == null) return false;
        Matcher matcher = pattern.matcher(input.trim());
        return matcher.matches();
    }

    // ── validate methods (throw on failure) ──────────────────────────────────

    public static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        if (!isValidEmail.test(email)) {
            throw new IllegalArgumentException(
                    "Invalid email format. Please enter a valid address (e.g., name@example.com)");
        }
    }

    public static void validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number must not be blank");
        }
        if (!isValidAccountNumber.test(accountNumber)) {
            throw new IllegalArgumentException(
                    "Invalid account number format. Expected format: ACC001");
        }
    }

    public static void validateCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID must not be blank");
        }
        if (!isValidCustomerId.test(customerId)) {
            throw new IllegalArgumentException(
                    "Invalid customer ID format. Expected format: CUST001");
        }
    }

    public static void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact must not be blank");
        }
        if (!isValidPhone.test(phone)) {
            throw new IllegalArgumentException(
                    "Contact must be a valid phone number (7-15 digits; may include +, -, spaces, or parentheses)");
        }
    }

    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        if (!isValidName.test(name)) {
            throw new IllegalArgumentException(
                    "Name must contain only letters, spaces, hyphens, or apostrophes");
        }
    }
}
