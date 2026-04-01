package com.bank_management_system.accounts;

import com.bank_management_system.customers.Customer;
import com.bank_management_system.exceptions.InsufficientFundsException;

/**
 * A savings account that earns interest and enforces a minimum balance.
 * Withdrawals that would drop the balance below {@code MINIMUM_BALANCE} are rejected
 * with an {@link com.bank_management_system.exceptions.InsufficientFundsException}.
 */
public class SavingsAccount extends Account {

    private static final double MINIMUM_BALANCE = 500.0;
    private final double interestRate;

    public SavingsAccount(Customer customer, double balance) {
        super(customer, balance);
        interestRate = 0.035;
    }

    /** Restoration constructor — rebuilds from file data with an explicit account number and status. */
    public SavingsAccount(String accountNumber, Customer customer, double balance, String status) {
        super(accountNumber, customer, balance, status);
        interestRate = 0.035;
    }

    /**
     * Calculates the interest amount due based on the current balance and interest rate.
     *
     * @return the interest amount to be credited
     */
    public double calculateInterest() {
        return getBalance() * interestRate;
    }

    /**
     * Returns the annual interest rate applied to this account.
     *
     * @return the interest rate as a decimal (e.g. 0.035 for 3.5%)
     */
    public double getInterestRate() { return interestRate; }

    /**
     * Returns the minimum balance that must be maintained in a Savings account.
     *
     * @return the minimum balance amount
     */
    public static double getMinimumBalance() { return MINIMUM_BALANCE; }

    /**
     * Prints a formatted one-line summary of this account's details.
     */
    @Override
    public void displayAccountDetails() {
        System.out.printf("  %-8s | %-20s | Savings  | $%,12.2f | %s%n",
                getAccountNumber(), getCustomerName(), getBalance(), getStatus());
        System.out.printf("    Interest Rate: %.1f%% | Min Balance: $%.2f%n",
                interestRate * 100, MINIMUM_BALANCE);
    }

    /**
     * Returns the account type label.
     *
     * @return "Savings"
     */
    @Override
    public String getAccountType() { return "Savings"; }

    /**
     * Serializes this account to a pipe-delimited line for file persistence.
     * Format: SAVINGS|accNum|custId|custName|custType|age|contact|email|address|balance|status
     *
     * @return the serialized file line
     */
    @Override
    public String toFileLine() {
        return String.join("|",
                "SAVINGS",
                getAccountNumber(),
                getCustomerId(),
                getCustomerName(),
                getCustomer().getCustomerType(),
                String.valueOf(getCustomer().getAge()),
                getCustomer().getContact(),
                getCustomer().getEmail(),
                getCustomer().getAddress(),
                String.valueOf(getBalance()),
                getStatus());
    }

    /**
     * Ensures the withdrawal would not push the balance below {@code MINIMUM_BALANCE}.
     *
     * @param amount the requested withdrawal amount
     * @throws com.bank_management_system.exceptions.InsufficientFundsException if the minimum balance would be breached
     */
    @Override
    protected void validateWithdrawal(double amount) {
        if (getBalance() - amount < MINIMUM_BALANCE) {
            throw new InsufficientFundsException(String.format(
                    "Withdrawal denied. Savings account must maintain a minimum balance of $%.2f. " +
                    "Current balance: $%.2f, requested: $%.2f.",
                    MINIMUM_BALANCE, getBalance(), amount));
        }
    }
}
