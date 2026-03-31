package com.bank_management_system.accounts;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.bank_management_system.bank.Bank;
import com.bank_management_system.customers.Customer;
import com.bank_management_system.exceptions.IllegalStateException;
import com.bank_management_system.exceptions.InvalidAmountException;
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
     * Transfers a given amount from one account to another.
     * Records a TRANSFER_OUT transaction on the source and a TRANSFER_IN on the destination.
     *
     * @param fromAccountNumber the account to debit
     * @param toAccountNumber   the account to credit
     * @param amount            the amount to transfer (must be greater than zero)
     * @throws com.bank_management_system.exceptions.IllegalArgumentException if both account numbers are the same
     * @throws com.bank_management_system.exceptions.IllegalStateException    if either account is closed
     * @throws com.bank_management_system.exceptions.InsufficientFundsException if the source lacks sufficient funds
     */
    public void transfer(String fromAccountNumber, String toAccountNumber, double amount) {
        if (fromAccountNumber.equalsIgnoreCase(toAccountNumber)) {
            throw new com.bank_management_system.exceptions.IllegalArgumentException(
                    "Cannot transfer to the same account.");
        }

        Account source      = accountManager.findAccountOrThrow(fromAccountNumber);
        Account destination = accountManager.findAccountOrThrow(toAccountNumber);

        if ("Closed".equalsIgnoreCase(destination.getStatus())) {
            throw new com.bank_management_system.exceptions.IllegalStateException(
                    "Cannot transfer to a closed account: " + toAccountNumber);
        }

        source.withdraw(amount);
        destination.deposit(amount);

        transactionManager.addTransaction(
                new Transaction(fromAccountNumber, "TRANSFER_OUT", amount, source.getBalance()));
        transactionManager.addTransaction(
                new Transaction(toAccountNumber, "TRANSFER_IN", amount, destination.getBalance()));
    }

    /**
     * Processes a deposit or withdrawal on the specified account and records the transaction.
     *
     * @param accountNumber the account to transact on
     * @param amount        the transaction amount
     * @param type          "DEPOSIT" or "WITHDRAWAL"
     * @throws com.bank_management_system.exceptions.IllegalArgumentException if the type is unrecognised
     */
    public void processTransaction(String accountNumber, double amount, String type) {
        Account account = accountManager.findAccountOrThrow(accountNumber);
        Transaction transaction;

        if ("DEPOSIT".equalsIgnoreCase(type)) {
            transaction = account.deposit(amount);
        } else if ("WITHDRAWAL".equalsIgnoreCase(type)) {
            transaction = account.withdraw(amount);
        } else {
            throw new com.bank_management_system.exceptions.IllegalArgumentException(
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
     * Displays the transaction history for the given account, sorted by the chosen field.
     *
     * @param accountNumber the account number to look up
     * @param sortBy        "DATE" for newest-first, "AMOUNT" for highest-amount-first
     */
    public void getTransactionHistory(String accountNumber, String sortBy) {
        accountManager.findAccountOrThrow(accountNumber);

        Comparator<Transaction> sortOrder = sortBy.equalsIgnoreCase("AMOUNT")
                ? Comparator.comparingDouble(Transaction::getAmount).reversed()
                : Comparator.comparing(Transaction::getCreatedAt).reversed();

        transactionManager.viewTransactionsByAccount(accountNumber, sortOrder);
    }

    /**
     * Displays a formatted table of all accounts in the system.
     */
    public void displayAllAccounts() {
        accountManager.viewAllAccounts();
    }

    /** Returns all accounts currently held in memory. */
    public Collection<Account> getAllAccounts() {
        return accountManager.getAccounts();
    }

    /** Returns all transactions currently held in memory. */
    public List<Transaction> getAllTransactions() {
        return transactionManager.getAllTransactions();
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
        Predicate<Account> activeChecking = account ->
                account instanceof CheckingAccount && account.getStatus().equalsIgnoreCase("Active");

        List<Transaction> fees = accountManager.findAccountsMatching(activeChecking).stream()
                .map(CheckingAccount.class::cast)
                .map(CheckingAccount::applyMonthlyFee)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        fees.forEach(transactionManager::addTransaction);
        return fees.size();
    }

    /**
     * Credits the applicable interest to every active Savings account
     * and records each credit as a deposit transaction.
     *
     * @return the number of accounts credited
     */
    public int applyInterest() {
        Predicate<Account> activeSavings = account ->
                account instanceof SavingsAccount && account.getStatus().equalsIgnoreCase("Active");

        List<Account> savingsAccounts = accountManager.findAccountsMatching(activeSavings);

        savingsAccounts.forEach(account -> {
            SavingsAccount sa = (SavingsAccount) account;
            transactionManager.addTransaction(account.deposit(sa.calculateInterest()));
        });

        return savingsAccounts.size();
    }
}
