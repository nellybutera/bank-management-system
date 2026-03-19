package com.bank_management_system;

import com.bank_management_system.shared.IllegalArgumentException;
import com.bank_management_system.shared.InputValidator;
import com.bank_management_system.shared.InvalidAmountException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InputValidator — covers every validator method for both
 * valid inputs (must not throw) and invalid inputs (must throw the correct
 * exception type).
 *
 * No @BeforeEach is needed because all methods are static and hold no state.
 */
@ExtendWith(TestResultLogger.class)
class InputValidatorTest {

    // -------------------------------------------------------------------------
    // validateName()
    // -------------------------------------------------------------------------

    @Test
    void validNamePasses() {
        assertDoesNotThrow(() -> InputValidator.validateName("John Smith"));
    }

    @Test
    void nameWithApostrophePasses() {
        assertDoesNotThrow(() -> InputValidator.validateName("O'Brien"));
    }

    @Test
    void nameWithHyphenPasses() {
        assertDoesNotThrow(() -> InputValidator.validateName("Mary-Jane"));
    }

    @Test
    void blankNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validateName("   "));
    }

    @Test
    void nameWithDigitsThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validateName("John123"));
    }

    @Test
    void nameWithSpecialCharsThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validateName("John@Doe!"));
    }

    // -------------------------------------------------------------------------
    // validateAge()
    // -------------------------------------------------------------------------

    @Test
    void validAgePasses() {
        assertDoesNotThrow(() -> InputValidator.validateAge(25));
    }

    @Test
    void minimumAgePasses() {
        assertDoesNotThrow(() -> InputValidator.validateAge(18));
    }

    @Test
    void maximumAgePasses() {
        assertDoesNotThrow(() -> InputValidator.validateAge(120));
    }

    @Test
    void ageBelowMinimumThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validateAge(17));
    }

    @Test
    void ageAboveMaximumThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validateAge(121));
    }

    // -------------------------------------------------------------------------
    // validateContact()
    // -------------------------------------------------------------------------

    @Test
    void validPhoneNumberPasses() {
        assertDoesNotThrow(() -> InputValidator.validateContact("555-1234"));
    }

    @Test
    void internationalFormatPasses() {
        assertDoesNotThrow(() -> InputValidator.validateContact("+1 555-1234"));
    }

    @Test
    void tooShortContactThrowsException() {
        // fewer than 7 characters
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validateContact("123"));
    }

    @Test
    void lettersInContactThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validateContact("abcdefgh"));
    }

    // -------------------------------------------------------------------------
    // validateAddress()
    // -------------------------------------------------------------------------

    @Test
    void validAddressPasses() {
        assertDoesNotThrow(() -> InputValidator.validateAddress("123 Main St, Springfield"));
    }

    @Test
    void blankAddressThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validateAddress("   "));
    }

    @Test
    void addressWithInvalidCharsThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validateAddress("123 Main @#$"));
    }

    // -------------------------------------------------------------------------
    // validateAccountNumber()
    // -------------------------------------------------------------------------

    @Test
    void validAccountNumberPasses() {
        assertDoesNotThrow(() -> InputValidator.validateAccountNumber("ACC001"));
    }

    @Test
    void lowercaseAccountNumberPasses() {
        // format check is case-insensitive
        assertDoesNotThrow(() -> InputValidator.validateAccountNumber("acc001"));
    }

    @Test
    void accountNumberWrongPrefixThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validateAccountNumber("AC001"));
    }

    @Test
    void accountNumberWithNoDigitsThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validateAccountNumber("ACC"));
    }

    @Test
    void blankAccountNumberThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validateAccountNumber("   "));
    }

    // -------------------------------------------------------------------------
    // validateCustomerId()
    // -------------------------------------------------------------------------

    @Test
    void validCustomerIdPasses() {
        assertDoesNotThrow(() -> InputValidator.validateCustomerId("CUST001"));
    }

    @Test
    void lowercaseCustomerIdPasses() {
        assertDoesNotThrow(() -> InputValidator.validateCustomerId("cust001"));
    }

    @Test
    void invalidCustomerIdPrefixThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validateCustomerId("CUS001"));
    }

    @Test
    void blankCustomerIdThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validateCustomerId("   "));
    }

    // -------------------------------------------------------------------------
    // validateAmount()
    // -------------------------------------------------------------------------

    @Test
    void validAmountPasses() {
        assertDoesNotThrow(() -> InputValidator.validateAmount(100.0));
    }

    @Test
    void zeroAmountThrowsInvalidAmountException() {
        assertThrows(InvalidAmountException.class, () -> InputValidator.validateAmount(0));
    }

    @Test
    void negativeAmountThrowsInvalidAmountException() {
        assertThrows(InvalidAmountException.class, () -> InputValidator.validateAmount(-50.0));
    }

    // -------------------------------------------------------------------------
    // validateMenuChoice()
    // -------------------------------------------------------------------------

    @Test
    void validMenuChoicePasses() {
        assertDoesNotThrow(() -> InputValidator.validateMenuChoice(3, 1, 5));
    }

    @Test
    void minimumMenuChoicePasses() {
        assertDoesNotThrow(() -> InputValidator.validateMenuChoice(1, 1, 5));
    }

    @Test
    void maximumMenuChoicePasses() {
        assertDoesNotThrow(() -> InputValidator.validateMenuChoice(5, 1, 5));
    }

    @Test
    void choiceBelowMinThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validateMenuChoice(0, 1, 5));
    }

    @Test
    void choiceAboveMaxThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validateMenuChoice(6, 1, 5));
    }
}
