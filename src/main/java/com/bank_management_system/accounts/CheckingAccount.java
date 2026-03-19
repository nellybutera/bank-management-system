package com.bank_management_system.accounts;

import com.bank_management_system.customers.Customer;
import com.bank_management_system.shared.InsufficientFundsException;
import com.bank_management_system.shared.OverdraftLimitExceededException;
import com.bank_management_system.transactions.Transaction;

public class CheckingAccount extends Account {

    private double overdraftLimit;
    private double monthlyFee;
    private final boolean feesWaived;

    public CheckingAccount(Customer customer, double balance, boolean feesWaived) {
        super(customer, balance);
        overdraftLimit = 1000;
        monthlyFee = 10;
        this.feesWaived = feesWaived;
    }

    /**
     * Returns whether the monthly fee is waived for this account.
     *
     * @return true if fees are waived (Premium customers), false otherwise
     */
    public boolean isFeesWaived() { return feesWaived; }

    /**
     * Returns the account type label.
     *
     * @return "Checking"
     */
    @Override
    public String getAccountType() { return "Checking"; }

    /**
     * Deducts the monthly fee from the account balance if fees are not waived.
     *
     * @return the resulting Transaction, or null if fees are waived
     */
    public Transaction applyMonthlyFee() {
        if (!feesWaived) {
            return withdraw(monthlyFee);
        }
        return null;
    }

    /**
     * Prints a formatted one-line summary of this account's details.
     */
    @Override
    public void displayAccountDetails() {
        System.out.printf("  %-8s | %-20s | Checking | $%,12.2f | %s%n",
                getAccountNumber(), getCustomerName(), getBalance(), getStatus());
        System.out.printf("    Overdraft Limit: $%,.2f | Monthly Fee: $%.2f%s%n",
                overdraftLimit, monthlyFee, feesWaived ? " (Waived)" : "");
    }

    @Override
    protected void validateWithdrawal(double amount) throws OverdraftLimitExceededException {
        if (getBalance() - amount < -overdraftLimit) {
            throw new OverdraftLimitExceededException(String.format(
                    "Withdrawal denied. Overdraft limit of $%.2f exceeded. " +
                    "Current balance: $%.2f, requested: $%.2f.",
                    overdraftLimit, getBalance(), amount));
        }
    }
}
