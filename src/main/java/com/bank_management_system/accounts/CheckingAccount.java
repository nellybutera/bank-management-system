package com.bank_management_system.accounts;

import com.bank_management_system.customers.Customer;
import com.bank_management_system.exceptions.OverdraftLimitExceededException;
import com.bank_management_system.transactions.Transaction;

/**
 * A checking account that supports overdraft up to {@code OVERDRAFT_LIMIT}
 * and charges a flat monthly fee (waived for Premium customers).
 */
public class CheckingAccount extends Account {

    private static final double OVERDRAFT_LIMIT = 1000.0;
    private static final double MONTHLY_FEE     = 10.0;

    private final boolean feesWaived;

    public CheckingAccount(Customer customer, double balance, boolean feesWaived) {
        super(customer, balance);
        this.feesWaived = feesWaived;
    }

    /** Restoration constructor — rebuilds from file data with an explicit account number and status. */
    public CheckingAccount(String accountNumber, Customer customer, double balance, String status, boolean feesWaived) {
        super(accountNumber, customer, balance, status);
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
     * Serializes this account to a pipe-delimited line for file persistence.
     * Format: CHECKING|accNum|custId|custName|custType|age|contact|email|address|balance|status|feesWaived
     *
     * @return the serialized file line
     */
    @Override
    public String toFileLine() {
        return String.join("|",
                "CHECKING",
                getAccountNumber(),
                getCustomerId(),
                getCustomerName(),
                getCustomer().getCustomerType(),
                String.valueOf(getCustomer().getAge()),
                getCustomer().getContact(),
                getCustomer().getEmail(),
                getCustomer().getAddress(),
                String.valueOf(getBalance()),
                getStatus(),
                String.valueOf(feesWaived));
    }

    /**
     * Deducts the monthly fee from the account balance if fees are not waived.
     *
     * @return the resulting Transaction, or null if fees are waived
     */
    public Transaction applyMonthlyFee() {
        if (!feesWaived) {
            return withdraw(MONTHLY_FEE);
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
                OVERDRAFT_LIMIT, MONTHLY_FEE, feesWaived ? " (Waived)" : "");
    }

    /**
     * Ensures the withdrawal would not push the balance beyond the overdraft limit.
     *
     * @param amount the requested withdrawal amount
     * @throws OverdraftLimitExceededException if the withdrawal would exceed the overdraft limit
     */
    @Override
    protected void validateWithdrawal(double amount) throws OverdraftLimitExceededException {
        if (getBalance() - amount < -OVERDRAFT_LIMIT) {
            throw new OverdraftLimitExceededException(String.format(
                    "Withdrawal denied. Overdraft limit of $%.2f exceeded. " +
                    "Current balance: $%.2f, requested: $%.2f.",
                    OVERDRAFT_LIMIT, getBalance(), amount));
        }
    }
}
