package com.bank_management_system;

import com.bank_management_system.accounts.CheckingAccount;
import com.bank_management_system.accounts.SavingsAccount;
import com.bank_management_system.customers.RegularCustomer;
import com.bank_management_system.shared.IllegalStateException;
import com.bank_management_system.shared.InsufficientFundsException;
import com.bank_management_system.shared.InvalidAmountException;
import com.bank_management_system.shared.OverdraftLimitExceededException;
import com.bank_management_system.transactions.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for deposit() and withdraw() on SavingsAccount and CheckingAccount.
 *
 * Each test method validates one specific behaviour — valid operations,
 * balance updates, returned Transaction records, or expected exceptions.
 *
 * @BeforeEach resets a fresh customer, savings account, and checking account
 * before every test so tests do not affect each other.
 */
@ExtendWith(TestResultLogger.class)
class AccountTest {

    private RegularCustomer customer;
    private SavingsAccount savings;
    private CheckingAccount checking;

    @BeforeEach
    void setUp() {
        customer = new RegularCustomer("Test User", 30, "555-0000", "123 Test St");
        savings  = new SavingsAccount(customer, 1000.00);
        checking = new CheckingAccount(customer, 500.00, false);
    }

    // -------------------------------------------------------------------------
    // deposit() — valid cases
    // -------------------------------------------------------------------------

    @Test
    void depositUpdatesBalance() {
        savings.deposit(200.00);
        assertEquals(1200.00, savings.getBalance(), 0.001);
    }

    @Test
    void depositReturnsTransactionRecord() {
        Transaction t = savings.deposit(300.00);
        assertNotNull(t);
        assertEquals("DEPOSIT", t.getType());
        assertEquals(300.00, t.getAmount(), 0.001);
        assertEquals(savings.getAccountNumber(), t.getAccountNumber());
    }

    // -------------------------------------------------------------------------
    // deposit() — invalid cases
    // -------------------------------------------------------------------------

    @Test
    void depositZeroThrowsInvalidAmountException() {
        assertThrows(InvalidAmountException.class, () -> savings.deposit(0));
    }

    @Test
    void depositNegativeThrowsInvalidAmountException() {
        assertThrows(InvalidAmountException.class, () -> savings.deposit(-50.00));
    }

    @Test
    void depositToClosedAccountThrowsIllegalStateException() {
        savings.closeAccount();
        assertThrows(IllegalStateException.class, () -> savings.deposit(100.00));
    }

    // -------------------------------------------------------------------------
    // withdraw() — valid cases
    // -------------------------------------------------------------------------

    @Test
    void withdrawUpdatesBalance() {
        savings.withdraw(200.00);
        assertEquals(800.00, savings.getBalance(), 0.001);
    }

    @Test
    void withdrawReturnsTransactionRecord() {
        Transaction t = savings.withdraw(100.00);
        assertNotNull(t);
        assertEquals("WITHDRAWAL", t.getType());
        assertEquals(100.00, t.getAmount(), 0.001);
        assertEquals(savings.getAccountNumber(), t.getAccountNumber());
    }

    @Test
    void withdrawExactMinimumBalanceAllowed() {
        // $1000 - $500 = $500 which equals minimum — should succeed
        savings.withdraw(500.00);
        assertEquals(500.00, savings.getBalance(), 0.001);
    }

    // -------------------------------------------------------------------------
    // withdraw() — invalid cases (SavingsAccount)
    // -------------------------------------------------------------------------

    @Test
    void withdrawBelowMinimumThrowsException() {
        // $1000 - $600 = $400 which is below the $500 minimum balance
        assertThrows(InsufficientFundsException.class, () -> savings.withdraw(600.00));
    }

    @Test
    void withdrawZeroThrowsInvalidAmountException() {
        assertThrows(InvalidAmountException.class, () -> savings.withdraw(0));
    }

    @Test
    void withdrawFromClosedAccountThrowsIllegalStateException() {
        savings.closeAccount();
        assertThrows(IllegalStateException.class, () -> savings.withdraw(100.00));
    }

    // -------------------------------------------------------------------------
    // CheckingAccount — overdraft rules
    // -------------------------------------------------------------------------

    @Test
    void overdraftWithinLimitAllowed() {
        // $500 balance - $1400 withdrawal = -$900, which is within the -$1000 limit
        checking.withdraw(1400.00);
        assertEquals(-900.00, checking.getBalance(), 0.001);
    }

    @Test
    void overdraftExceedThrowsOverdraftLimitExceededException() {
        // $500 balance - $1600 withdrawal = -$1100, which exceeds the -$1000 limit
        assertThrows(OverdraftLimitExceededException.class, () -> checking.withdraw(1600.00));
    }

    @Test
    void overdraftLimitExceededIsSubtypeOfInsufficientFunds() {
        // OverdraftLimitExceededException extends InsufficientFundsException,
        // so any existing catch block for InsufficientFundsException still covers it
        assertThrows(InsufficientFundsException.class, () -> checking.withdraw(1600.00));
    }
}
