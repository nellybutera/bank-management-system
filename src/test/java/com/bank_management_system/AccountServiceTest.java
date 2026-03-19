package com.bank_management_system;

import com.bank_management_system.accounts.Account;
import com.bank_management_system.accounts.AccountManager;
import com.bank_management_system.accounts.AccountService;
import com.bank_management_system.accounts.CheckingAccount;
import com.bank_management_system.accounts.SavingsAccount;
import com.bank_management_system.bank.Bank;
import com.bank_management_system.customers.PremiumCustomer;
import com.bank_management_system.customers.RegularCustomer;
import com.bank_management_system.shared.AccountNotFoundException;
import com.bank_management_system.shared.IllegalArgumentException;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AccountService using Mockito to isolate it from its dependencies.
 *
 * Bank, AccountManager, and TransactionManager are all replaced with mocks so
 * each test controls exactly what those dependencies return and can verify
 * exactly how AccountService interacts with them — without any real storage.
 *
 * Key Mockito patterns used:
 *   @Mock          — creates a mock object (a fake with controllable behaviour)
 *   @InjectMocks   — creates AccountService and injects the mocks via its constructor
 *   when().thenReturn()  — stubs a method to return a specific value
 *   when().thenThrow()   — stubs a method to throw an exception
 *   verify()             — asserts that a method was called (and how many times)
 *   verify(_, never())   — asserts that a method was NEVER called
 */
@ExtendWith({MockitoExtension.class, TestResultLogger.class})
class AccountServiceTest {

    // --- Mocks (fakes injected into AccountService) --------------------------

    @Mock
    private Bank bank;

    @Mock
    private AccountManager accountManager;

    @Mock
    private TransactionManager transactionManager;

    // AccountService receives the three mocks above through its constructor
    @InjectMocks
    private AccountService accountService;

    // --- Real objects used as test data --------------------------------------

    private RegularCustomer regularCustomer;
    private PremiumCustomer premiumCustomer;
    private SavingsAccount  savingsAccount;

    @BeforeEach
    void setUp() {
        regularCustomer = new RegularCustomer("Test User", 30, "555-0000", "123 Test St");
        premiumCustomer = new PremiumCustomer("VIP User",  40, "555-1111", "456 VIP Ave");
        savingsAccount  = new SavingsAccount(regularCustomer, 1000.00);
    }

    // -------------------------------------------------------------------------
    // processTransaction() — routing and ledger logging
    // -------------------------------------------------------------------------

    /**
     * When a DEPOSIT is processed, AccountService must log the resulting
     * Transaction by calling addTransaction() on TransactionManager exactly once.
     *
     * Mockito pattern: stub → act → verify interaction
     */
    @Test
    void depositLogsTransactionInLedger() {
        when(accountManager.findAccountOrThrow(savingsAccount.getAccountNumber()))
                .thenReturn(savingsAccount);

        accountService.processTransaction(savingsAccount.getAccountNumber(), 200.00, "DEPOSIT");

        // verify that the ledger received exactly one transaction
        verify(transactionManager, times(1)).addTransaction(any(Transaction.class));
    }

    /**
     * Same check for WITHDRAWAL — the ledger must always be updated
     * regardless of whether money went in or out.
     */
    @Test
    void withdrawalLogsTransactionInLedger() {
        when(accountManager.findAccountOrThrow(savingsAccount.getAccountNumber()))
                .thenReturn(savingsAccount);

        accountService.processTransaction(savingsAccount.getAccountNumber(), 100.00, "WITHDRAWAL");

        verify(transactionManager, times(1)).addTransaction(any(Transaction.class));
    }

    /**
     * An unrecognised transaction type (anything other than DEPOSIT or WITHDRAWAL)
     * must throw IllegalArgumentException before any ledger write happens.
     *
     * Mockito pattern: verify(_, never()) — the mock lets us confirm addTransaction
     * was NEVER called when the type guard fires.
     */
    @Test
    void invalidTransactionTypeThrowsIllegalArgumentException() {
        when(accountManager.findAccountOrThrow(anyString())).thenReturn(savingsAccount);

        assertThrows(IllegalArgumentException.class,
                () -> accountService.processTransaction(savingsAccount.getAccountNumber(), 100.00, "TRANSFER"));

        // nothing should have been written to the ledger
        verify(transactionManager, never()).addTransaction(any());
    }

    /**
     * When AccountManager cannot find the account it throws AccountNotFoundException.
     * AccountService must let that exception propagate — it must not swallow it.
     *
     * Mockito pattern: when().thenThrow() — the mock throws on demand so we can
     * test the error path without needing a real storage array.
     */
    @Test
    void unknownAccountThrowsAccountNotFoundException() {
        when(accountManager.findAccountOrThrow("ACC999"))
                .thenThrow(new AccountNotFoundException("Account not found: ACC999"));

        assertThrows(AccountNotFoundException.class,
                () -> accountService.processTransaction("ACC999", 100.00, "DEPOSIT"));
    }

    // -------------------------------------------------------------------------
    // createSavingsAccount()
    // -------------------------------------------------------------------------

    /**
     * After successfully creating a savings account, AccountService must
     * register it with AccountManager by calling addAccount() exactly once.
     *
     * Mockito pattern: stub bank lookup, then verify the manager was updated.
     */
    @Test
    void createSavingsAccountRegistersWithAccountManager() {
        when(bank.findCustomerById(regularCustomer.getCustomerId()))
                .thenReturn(regularCustomer);

        accountService.createSavingsAccount(regularCustomer.getCustomerId(), 1000.00);

        verify(accountManager, times(1)).addAccount(any(SavingsAccount.class));
    }

    /**
     * An initial balance below the $500 minimum must throw InvalidAmountException
     * before AccountManager or the customer's account list is ever touched.
     */
    @Test
    void createSavingsAccountBelowMinimumThrowsException() {
        when(bank.findCustomerById(regularCustomer.getCustomerId()))
                .thenReturn(regularCustomer);

        assertThrows(InvalidAmountException.class,
                () -> accountService.createSavingsAccount(regularCustomer.getCustomerId(), 100.00));

        // the account was rejected — AccountManager must not have been updated
        verify(accountManager, never()).addAccount(any());
    }

    // -------------------------------------------------------------------------
    // createCheckingAccount()
    // -------------------------------------------------------------------------

    /**
     * A CheckingAccount created for a PremiumCustomer must have fees waived.
     * AccountService reads isEligibleForFeeWaiver() from the customer and passes
     * the result to the CheckingAccount constructor.
     *
     * Mockito pattern: stub bank to return a premium customer, then assert on
     * the returned account's state.
     */
    @Test
    void createCheckingAccountWaivesFeeForPremiumCustomer() {
        when(bank.findCustomerById(premiumCustomer.getCustomerId()))
                .thenReturn(premiumCustomer);

        CheckingAccount account =
                accountService.createCheckingAccount(premiumCustomer.getCustomerId(), 500.00);

        assertTrue(account.isFeesWaived(),
                "Checking account created for a Premium customer must have fees waived");
    }

    /**
     * A CheckingAccount created for a RegularCustomer must NOT have fees waived.
     */
    @Test
    void createCheckingAccountDoesNotWaiveFeeForRegularCustomer() {
        when(bank.findCustomerById(regularCustomer.getCustomerId()))
                .thenReturn(regularCustomer);

        CheckingAccount account =
                accountService.createCheckingAccount(regularCustomer.getCustomerId(), 500.00);

        assertFalse(account.isFeesWaived(),
                "Checking account created for a Regular customer must not have fees waived");
    }

    // -------------------------------------------------------------------------
    // closeAccount()
    // -------------------------------------------------------------------------

    /**
     * Attempting to close an account that still holds a balance must throw
     * IllegalStateException. The mock gives us precise control over what
     * findAccountOrThrow() returns so we can test this guard without
     * touching a real AccountManager array.
     */
    @Test
    void closeAccountWithNonZeroBalanceThrowsIllegalStateException() {
        // savingsAccount has $1000 — the close guard should fire
        when(accountManager.findAccountOrThrow(savingsAccount.getAccountNumber()))
                .thenReturn(savingsAccount);

        assertThrows(IllegalStateException.class,
                () -> accountService.closeAccount(savingsAccount.getAccountNumber()));
    }

    // -------------------------------------------------------------------------
    // getTransactionHistory()
    // -------------------------------------------------------------------------

    /**
     * getTransactionHistory() must first confirm the account exists (via
     * AccountManager) and then delegate the display to TransactionManager.
     * Both calls are mandatory — skipping either would be a bug.
     *
     * Mockito pattern: verify two separate dependencies were each called once.
     */
    @Test
    void getTransactionHistoryCallsBothManagerAndLedger() {
        when(accountManager.findAccountOrThrow(savingsAccount.getAccountNumber()))
                .thenReturn(savingsAccount);

        accountService.getTransactionHistory(savingsAccount.getAccountNumber());

        verify(accountManager,      times(1)).findAccountOrThrow(savingsAccount.getAccountNumber());
        verify(transactionManager,  times(1)).viewTransactionsByAccount(savingsAccount.getAccountNumber());
    }

    // -------------------------------------------------------------------------
    // applyMonthlyFees()
    // -------------------------------------------------------------------------

    /**
     * A non-waived CheckingAccount must have the $10 fee deducted and logged.
     * accountManager.getAccounts() returns a real CheckingAccount so the fee
     * logic inside applyMonthlyFees() runs against real account state.
     */
    @Test
    void applyMonthlyFeesChargesNonWaivedCheckingAccount() {
        CheckingAccount checking = new CheckingAccount(regularCustomer, 500.00, false);
        when(accountManager.getAccounts()).thenReturn(new Account[]{checking});

        int count = accountService.applyMonthlyFees();

        assertEquals(1, count);
        verify(transactionManager, times(1)).addTransaction(any(Transaction.class));
    }

    /**
     * A waived CheckingAccount (Premium customer) must be skipped entirely —
     * no transaction logged, count stays at zero.
     */
    @Test
    void applyMonthlyFeesSkipsWaivedCheckingAccount() {
        CheckingAccount waived = new CheckingAccount(premiumCustomer, 500.00, true);
        when(accountManager.getAccounts()).thenReturn(new Account[]{waived});

        int count = accountService.applyMonthlyFees();

        assertEquals(0, count);
        verify(transactionManager, never()).addTransaction(any());
    }

    /**
     * A SavingsAccount must be ignored when applying monthly fees — it is not
     * a CheckingAccount so the instanceof check inside applyMonthlyFees() skips it.
     */
    @Test
    void applyMonthlyFeesIgnoresSavingsAccounts() {
        when(accountManager.getAccounts()).thenReturn(new Account[]{savingsAccount});

        int count = accountService.applyMonthlyFees();

        assertEquals(0, count);
        verify(transactionManager, never()).addTransaction(any());
    }

    // -------------------------------------------------------------------------
    // applyInterest()
    // -------------------------------------------------------------------------

    /**
     * An active SavingsAccount must receive its interest credit and have the
     * resulting deposit transaction logged to the ledger.
     */
    @Test
    void applyInterestCreditsSavingsAccount() {
        when(accountManager.getAccounts()).thenReturn(new Account[]{savingsAccount});

        int count = accountService.applyInterest();

        assertEquals(1, count);
        verify(transactionManager, times(1)).addTransaction(any(Transaction.class));
    }

    /**
     * A closed SavingsAccount must be skipped — interest is only credited to
     * active accounts, so the ledger must not be written to.
     */
    @Test
    void applyInterestSkipsClosedSavingsAccount() {
        savingsAccount.closeAccount();
        when(accountManager.getAccounts()).thenReturn(new Account[]{savingsAccount});

        int count = accountService.applyInterest();

        assertEquals(0, count);
        verify(transactionManager, never()).addTransaction(any());
    }

    /**
     * A CheckingAccount must be ignored when applying interest — only
     * SavingsAccounts qualify.
     */
    @Test
    void applyInterestIgnoresCheckingAccounts() {
        CheckingAccount checking = new CheckingAccount(regularCustomer, 500.00, false);
        when(accountManager.getAccounts()).thenReturn(new Account[]{checking});

        int count = accountService.applyInterest();

        assertEquals(0, count);
        verify(transactionManager, never()).addTransaction(any());
    }
}
