package com.bank_management_system;

import com.bank_management_system.accounts.SavingsAccount;
import com.bank_management_system.customers.RegularCustomer;
import com.bank_management_system.transactions.TransactionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TestResultLogger.class)
class TransactionManagerTest {

    private TransactionManager transactionManager;
    private SavingsAccount account;
    private String accountNumber;

    @BeforeEach
    void setUp() {
        transactionManager = new TransactionManager();
        RegularCustomer customer = new RegularCustomer("Test User", 25, "555-1111", "test@email.com", "456 Test Ave");
        account       = new SavingsAccount(customer, 1000.00);
        accountNumber = account.getAccountNumber();
    }

    @Test
    void addTransactionRecordsDeposit() {
        transactionManager.addTransaction(account.deposit(500.00));
        assertEquals(500.00, transactionManager.calculateTotalDeposits(accountNumber), 0.001);
    }

    @Test
    void calculateTotalDepositsAggregatesMultiple() {
        transactionManager.addTransaction(account.deposit(100.00));
        transactionManager.addTransaction(account.deposit(200.00));
        transactionManager.addTransaction(account.deposit(300.00));
        assertEquals(600.00, transactionManager.calculateTotalDeposits(accountNumber), 0.001);
    }

    @Test
    void depositsMinusWithdrawalsEqualsNetBalanceChange() {
        transactionManager.addTransaction(account.deposit(500.00));   // balance 1500
        transactionManager.addTransaction(account.withdraw(200.00));  // balance 1300

        double net = transactionManager.calculateTotalDeposits(accountNumber)
                   - transactionManager.calculateTotalWithdrawals(accountNumber);

        assertEquals(300.00, net, 0.001);
        assertEquals(1300.00, account.getBalance(), 0.001);
    }

    @Test
    void calculateTotalDepositsReturnsZeroForUnknownAccount() {
        assertEquals(0.0, transactionManager.calculateTotalDeposits("ACC999"), 0.001);
    }
}
