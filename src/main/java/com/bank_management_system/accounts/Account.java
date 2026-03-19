package com.bank_management_system.accounts;

import com.bank_management_system.customers.Customer;
import com.bank_management_system.shared.InsufficientFundsException;
import com.bank_management_system.shared.InvalidAmountException;
import com.bank_management_system.shared.IllegalStateException;
import com.bank_management_system.shared.Transactable;
import com.bank_management_system.transactions.Transaction;

public abstract class Account implements Transactable {

    private final String accountNumber;
    private final Customer customer;
    private double balance;
    private String status;

    private static int accountCounter = 1;

    public Account(Customer customer, double balance) {
        this.accountNumber = String.format("ACC%03d", accountCounter++);
        this.customer = customer;
        this.balance = balance;
        this.status = "Active";
    }

    /** Returns the unique account number (e.g. ACC001). */
    public String getAccountNumber() { return accountNumber; }

    /** Returns the customer who owns this account. */
    public Customer getCustomer() { return customer; }

    /** Returns the name of the customer who owns this account. */
    public String getCustomerName() { return customer.getName(); }

    /** Returns the ID of the customer who owns this account. */
    public String getCustomerId() { return customer.getCustomerId(); }

    /** Returns the current account balance. */
    public double getBalance() { return balance; }

    /** Returns the current account status (Active or Closed). */
    public String getStatus() { return status; }

    /** Prints a formatted summary of this account's details. */
    public abstract void displayAccountDetails();

    /** Returns the account type label (e.g. "Savings" or "Checking"). */
    public abstract String getAccountType();

    /**
     * Deposits the given amount into the account.
     *
     * @param amount the amount to deposit (must be greater than zero)
     * @return the resulting Transaction record
     * @throws IllegalStateException  if the account is closed
     * @throws InvalidAmountException if the amount is zero or negative
     */
    public final Transaction deposit(double amount) {
        if ("Closed".equalsIgnoreCase(this.status)) {
            throw new IllegalStateException("Cannot deposit into a closed account.");
        }
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit must be greater than zero");
        }
        balance += amount;
        return new Transaction(accountNumber, "DEPOSIT", amount, balance);
    }

    /**
     * Withdraws the given amount from the account.
     *
     * @param amount the amount to withdraw (must be greater than zero)
     * @return the resulting Transaction record
     * @throws IllegalStateException      if the account is closed
     * @throws InvalidAmountException     if the amount is zero or negative
     * @throws InsufficientFundsException if the withdrawal would violate account rules
     */
    public final Transaction withdraw(double amount) {
        if ("Closed".equalsIgnoreCase(this.status)) {
            throw new IllegalStateException("Cannot withdraw from a closed account.");
        }
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero.");
        }
        validateWithdrawal(amount);
        balance -= amount;
        return new Transaction(accountNumber, "WITHDRAWAL", amount, balance);
    }

    /**
     * Processes a deposit or withdrawal and returns whether it succeeded.
     *
     * @param amount the transaction amount
     * @param type   "DEPOSIT" or "WITHDRAWAL"
     * @return true if the transaction succeeded, false otherwise
     */
    @Override
    public boolean processTransaction(double amount, String type) {
        try {
            if ("deposit".equalsIgnoreCase(type)) {
                deposit(amount);
            } else if ("withdrawal".equalsIgnoreCase(type)) {
                withdraw(amount);
            } else {
                System.out.println("Invalid transaction type. Must be 'DEPOSIT' or 'WITHDRAWAL'.");
                return false;
            }
            return true;
        } catch (InvalidAmountException | InsufficientFundsException e) {
            System.out.println("Transaction failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Closes the account by setting its status to Closed.
     */
    public void closeAccount() {
        this.status = "Closed";
    }

    @Override
    public String toString() {
        return String.format("[%s] %s Account | Owner: %s | Balance: $%,.2f | Status: %s",
                accountNumber, getAccountType(), customer.getName(), balance, status);
    }

    /** Enforces account-specific withdrawal rules before the balance is modified. */
    protected abstract void validateWithdrawal(double amount);
}
