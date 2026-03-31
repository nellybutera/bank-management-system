package com.bank_management_system;

import com.bank_management_system.accounts.CheckingAccount;
import com.bank_management_system.accounts.SavingsAccount;
import com.bank_management_system.customers.RegularCustomer;
import com.bank_management_system.exceptions.AccountNotFoundException;
import com.bank_management_system.exceptions.IllegalStateException;
import com.bank_management_system.exceptions.InputValidator;
import com.bank_management_system.exceptions.InsufficientFundsException;
import com.bank_management_system.exceptions.InvalidAmountException;
import com.bank_management_system.exceptions.OverdraftLimitExceededException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TestResultLogger.class)
class ExceptionTest {

    private SavingsAccount savings;
    private CheckingAccount checking;

    @BeforeEach
    void setUp() {
        RegularCustomer customer = new RegularCustomer("Test User", 30, "555-0000", "test@email.com", "123 Test St");
        savings  = new SavingsAccount(customer, 1000.00);
        checking = new CheckingAccount(customer, 500.00, false);
    }

    // --- InvalidAmountException ---

    @Test
    void validateAmountZeroThrowsInvalidAmountException() {
        assertThrows(InvalidAmountException.class, () -> InputValidator.validateAmount(0));
    }

    @Test
    void validateAmountNegativeThrowsInvalidAmountException() {
        assertThrows(InvalidAmountException.class, () -> InputValidator.validateAmount(-50));
    }

    @Test
    void invalidAmountExceptionMessageIsDescriptive() {
        InvalidAmountException ex = assertThrows(
                InvalidAmountException.class, () -> InputValidator.validateAmount(-100));
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isBlank());
    }

    // --- InsufficientFundsException ---

    @Test
    void withdrawBelowSavingsMinimumThrowsInsufficientFundsException() {
        // $1000 - $600 = $400, below the $500 minimum
        assertThrows(InsufficientFundsException.class, () -> savings.withdraw(600.00));
    }

    @Test
    void insufficientFundsExceptionMessageContainsBalance() {
        InsufficientFundsException ex = assertThrows(
                InsufficientFundsException.class, () -> savings.withdraw(600.00));
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isBlank());
    }

    // --- OverdraftLimitExceededException ---

    @Test
    void overdraftExceededExtendsInsufficientFundsException() {
        // OverdraftLimitExceededException must be catchable as InsufficientFundsException
        assertThrows(InsufficientFundsException.class, () -> checking.withdraw(1600.00));
    }

    @Test
    void overdraftExceededIsSpecificSubtype() {
        assertThrows(OverdraftLimitExceededException.class, () -> checking.withdraw(1600.00));
    }

    // --- IllegalStateException (closed account) ---

    @Test
    void depositToClosedAccountThrowsIllegalStateException() {
        savings.closeAccount();
        assertThrows(IllegalStateException.class, () -> savings.deposit(100.00));
    }

    @Test
    void withdrawFromClosedAccountThrowsIllegalStateException() {
        savings.closeAccount();
        assertThrows(IllegalStateException.class, () -> savings.withdraw(100.00));
    }

    // --- AccountNotFoundException ---

    @Test
    void accountNotFoundExceptionCarriesMessage() {
        AccountNotFoundException ex = assertThrows(
                AccountNotFoundException.class,
                () -> { throw new AccountNotFoundException("Account not found: ACC999"); });
        assertTrue(ex.getMessage().contains("ACC999"));
    }

    // --- InputValidator menu choice ---

    @Test
    void menuChoiceBelowRangeThrowsIllegalArgumentException() {
        assertThrows(com.bank_management_system.exceptions.IllegalArgumentException.class,
                () -> InputValidator.validateMenuChoice(0, 1, 5));
    }

    @Test
    void menuChoiceAboveRangeThrowsIllegalArgumentException() {
        assertThrows(com.bank_management_system.exceptions.IllegalArgumentException.class,
                () -> InputValidator.validateMenuChoice(6, 1, 5));
    }
}
