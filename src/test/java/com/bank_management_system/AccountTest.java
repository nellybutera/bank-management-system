package com.bank_management_system;

import com.bank_management_system.accounts.CheckingAccount;
import com.bank_management_system.accounts.SavingsAccount;
import com.bank_management_system.customers.RegularCustomer;
import com.bank_management_system.shared.IllegalStateException;
import com.bank_management_system.shared.InsufficientFundsException;
import com.bank_management_system.shared.InvalidAmountException;
import com.bank_management_system.shared.OverdraftLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TestResultLogger.class)
class AccountTest {

    private SavingsAccount savings;
    private CheckingAccount checking;

    @BeforeEach
    void setUp() {
        RegularCustomer customer = new RegularCustomer("Test User", 30, "555-0000", "123 Test St");
        savings  = new SavingsAccount(customer, 1000.00);
        checking = new CheckingAccount(customer, 500.00, false);
    }

    // --- deposit() ---

    @Test
    void depositUpdatesBalance() {
        savings.deposit(200.00);
        assertEquals(1200.00, savings.getBalance(), 0.001);
    }

    @Test
    void depositZeroThrowsInvalidAmountException() {
        assertThrows(InvalidAmountException.class, () -> savings.deposit(0));
    }

    @Test
    void depositToClosedAccountThrowsIllegalStateException() {
        savings.closeAccount();
        assertThrows(IllegalStateException.class, () -> savings.deposit(100.00));
    }

    // --- withdraw() ---

    @Test
    void withdrawUpdatesBalance() {
        savings.withdraw(200.00);
        assertEquals(800.00, savings.getBalance(), 0.001);
    }

    @Test
    void withdrawBelowMinimumThrowsException() {
        // $1000 - $600 = $400, below the $500 minimum balance
        assertThrows(InsufficientFundsException.class, () -> savings.withdraw(600.00));
    }

    @Test
    void withdrawFromClosedAccountThrowsIllegalStateException() {
        savings.closeAccount();
        assertThrows(IllegalStateException.class, () -> savings.withdraw(100.00));
    }

    // --- CheckingAccount overdraft ---

    @Test
    void overdraftWithinLimitAllowed() {
        // $500 - $1400 = -$900, within the -$1000 limit
        checking.withdraw(1400.00);
        assertEquals(-900.00, checking.getBalance(), 0.001);
    }

    @Test
    void overdraftExceedThrowsOverdraftLimitExceededException() {
        // $500 - $1600 = -$1100, exceeds the -$1000 limit
        assertThrows(OverdraftLimitExceededException.class, () -> checking.withdraw(1600.00));
    }
}
