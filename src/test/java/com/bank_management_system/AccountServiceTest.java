package com.bank_management_system;

import com.bank_management_system.accounts.AccountManager;
import com.bank_management_system.accounts.AccountService;
import com.bank_management_system.accounts.CheckingAccount;
import com.bank_management_system.accounts.SavingsAccount;
import com.bank_management_system.bank.Bank;
import com.bank_management_system.customers.PremiumCustomer;
import com.bank_management_system.customers.RegularCustomer;
import com.bank_management_system.shared.AccountNotFoundException;
import com.bank_management_system.shared.IllegalStateException;
import com.bank_management_system.shared.InvalidAmountException;
import com.bank_management_system.transactions.Transaction;
import com.bank_management_system.transactions.TransactionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, TestResultLogger.class})
class AccountServiceTest {

    @Mock private Bank bank;
    @Mock private AccountManager accountManager;
    @Mock private TransactionManager transactionManager;

    @InjectMocks
    private AccountService accountService;

    private RegularCustomer regularCustomer;
    private PremiumCustomer premiumCustomer;
    private SavingsAccount  savingsAccount;

    @BeforeEach
    void setUp() {
        regularCustomer = new RegularCustomer("Test User", 30, "555-0000", "123 Test St");
        premiumCustomer = new PremiumCustomer("VIP User",  40, "555-1111", "456 VIP Ave");
        savingsAccount  = new SavingsAccount(regularCustomer, 1000.00);
    }

    @Test
    void depositLogsTransactionInLedger() {
        when(accountManager.findAccountOrThrow(savingsAccount.getAccountNumber()))
                .thenReturn(savingsAccount);

        accountService.processTransaction(savingsAccount.getAccountNumber(), 200.00, "DEPOSIT");

        verify(transactionManager, times(1)).addTransaction(any(Transaction.class));
    }

    @Test
    void unknownAccountThrowsAccountNotFoundException() {
        when(accountManager.findAccountOrThrow("ACC999"))
                .thenThrow(new AccountNotFoundException("Account not found: ACC999"));

        assertThrows(AccountNotFoundException.class,
                () -> accountService.processTransaction("ACC999", 100.00, "DEPOSIT"));
    }

    @Test
    void createSavingsAccountBelowMinimumThrowsException() {
        when(bank.findCustomerById(regularCustomer.getCustomerId())).thenReturn(regularCustomer);

        assertThrows(InvalidAmountException.class,
                () -> accountService.createSavingsAccount(regularCustomer.getCustomerId(), 100.00));

        verify(accountManager, never()).addAccount(any());
    }

    @Test
    void createCheckingAccountWaivesFeeForPremiumCustomer() {
        when(bank.findCustomerById(premiumCustomer.getCustomerId())).thenReturn(premiumCustomer);

        CheckingAccount account =
                accountService.createCheckingAccount(premiumCustomer.getCustomerId(), 500.00);

        assertTrue(account.isFeesWaived());
    }

    @Test
    void closeAccountWithNonZeroBalanceThrowsIllegalStateException() {
        when(accountManager.findAccountOrThrow(savingsAccount.getAccountNumber()))
                .thenReturn(savingsAccount);

        assertThrows(IllegalStateException.class,
                () -> accountService.closeAccount(savingsAccount.getAccountNumber()));
    }
}
