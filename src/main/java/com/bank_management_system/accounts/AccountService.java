package com.bank_management_system.accounts;

import com.bank_management_system.bank.Bank;
import com.bank_management_system.customers.Customer;
import com.bank_management_system.shared.InvalidAmountException;
import com.bank_management_system.shared.IllegalStateException;
import com.bank_management_system.transactions.Transaction;
import com.bank_management_system.transactions.TransactionManager;

public class AccountService {

    private final Bank bank;
    private final AccountManager accountManager;
    private final TransactionManager transactionManager;

    public AccountService(Bank bank, AccountManager accountManager, TransactionManager transactionManager) {
        this.bank = bank;
        this.accountManager = accountManager;
        this.transactionManager = transactionManager;
    }

    /**
     * Creates a new Savings account for the given customer with the specified initial balance.
     *
     * @param customerId     the ID of the customer opening the account
     * @param initialBalance the opening balance (must meet the minimum balance requirement)
     * @return the newly created SavingsAccount
     * @throws InvalidAmountException if the initial balance is below the minimum
     */
    public SavingsAccount createSavingsAccount(String customerId, double initialBalance) {
        Customer customer = bank.findCustomerById(customerId);

        if (initialBalance < SavingsAccount.getMinimumBalance()) {
            throw new InvalidAmountException(String.format(
                    "Initial balance for a Savings account must be at least $%.2f. Provided: $%.2f",
                    SavingsAccount.getMinimumBalance(), initialBalance));
        }

        SavingsAccount account = new SavingsAccount(customer, initialBalance);
        accountManager.addAccount(account);
        customer.addAccount(account);
        return account;
    }

    /**
     * Creates a new Checking account for the given customer with the specified initial balance.
     *
     * @param customerId     the ID of the customer opening the account
     * @param initialBalance the opening balance (must not be negative)
     * @return the newly created CheckingAccount
     * @throws InvalidAmountException if the initial balance is negative
     */
    public CheckingAccount createCheckingAccount(String customerId, double initialBalance) {
        Customer customer = bank.findCustomerById(customerId);

        if (initialBalance < 0) {
            throw new InvalidAmountException(
                    "Initial balance for a checking account cannot be negative. Provided: " + initialBalance);
        }

        boolean feesWaived = customer.isEligibleForFeeWaiver();
        CheckingAccount account = new CheckingAccount(customer, initialBalance, feesWaived);
        accountManager.addAccount(account);
        customer.addAccount(account);
        return account;
    }

    /**
     * Processes a deposit or withdrawal on the specified account and records the transaction.
     *
     * @param accountNumber the account to transact on
     * @param amount        the transaction amount
     * @param type          "DEPOSIT" or "WITHDRAWAL"
     * @throws com.bank_management_system.shared.IllegalArgumentException if the type is unrecognised
     */
    public void processTransaction(String accountNumber, double amount, String type) {
        Account account = accountManager.findAccountOrThrow(accountNumber);
        Transaction transaction;

        if ("DEPOSIT".equalsIgnoreCase(type)) {
            transaction = account.deposit(amount);
        } else if ("WITHDRAWAL".equalsIgnoreCase(type)) {
            transaction = account.withdraw(amount);
        } else {
            throw new com.bank_management_system.shared.IllegalArgumentException(
                    "Invalid transaction type: " + type + ". Must be DEPOSIT or WITHDRAWAL.");
        }

        transactionManager.addTransaction(transaction);
    }

    /**
     * Returns the account details for the given account number.
     *
     * @param accountNumber the account number to look up
     * @return the matching Account
     */
    public Account getAccountDetails(String accountNumber) {
        return accountManager.findAccountOrThrow(accountNumber);
    }

    /**
     * Displays the transaction history for the given account number.
     *
     * @param accountNumber the account number to look up
     */
    public void getTransactionHistory(String accountNumber) {
        accountManager.findAccountOrThrow(accountNumber);
        transactionManager.viewTransactionsByAccount(accountNumber);
    }

    /**
     * Displays a formatted table of all accounts in the system.
     */
    public void displayAllAccounts() {
        accountManager.viewAllAccounts();
    }

    /**
     * Closes the specified account by setting its status to Closed.
     * The account balance must be zero before closing.
     *
     * @param accountNumber the account number to close
     * @throws IllegalStateException if the account still has a non-zero balance
     */
    public void closeAccount(String accountNumber) {
        Account account = accountManager.findAccountOrThrow(accountNumber);

        if (account.getBalance() != 0) {
            throw new IllegalStateException("Withdraw all funds before closing.");
        }

        account.closeAccount();
        System.out.println("Account " + accountNumber + " is now closed");
    }

    /**
     * Deducts the monthly fee from every active, non-waived Checking account
     * and records each deduction as a transaction.
     *
     * @return the number of accounts charged
     */
    public int applyMonthlyFees() {
        Account[] allAccounts = accountManager.getAccounts();
        int chargedCount = 0;

        for (Account account : allAccounts) {
            if (account instanceof CheckingAccount ca && account.getStatus().equalsIgnoreCase("Active")) {
                Transaction transaction = ca.applyMonthlyFee();
                if (transaction != null) {
                    transactionManager.addTransaction(transaction);
                    chargedCount++;
                }
            }
        }
        return chargedCount;
    }

    /**
     * Credits the applicable interest to every active Savings account
     * and records each credit as a deposit transaction.
     *
     * @return the number of accounts credited
     */
    public int applyInterest() {
        Account[] allAccounts = accountManager.getAccounts();
        int creditedCount = 0;

        for (Account account : allAccounts) {
            if (account instanceof SavingsAccount sa && account.getStatus().equalsIgnoreCase("Active")) {
                double interest = sa.calculateInterest();
                Transaction transaction = account.deposit(interest);
                transactionManager.addTransaction(transaction);
                creditedCount++;
            }
        }
        return creditedCount;
    }
}
