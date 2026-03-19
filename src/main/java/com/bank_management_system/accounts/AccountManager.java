package com.bank_management_system.accounts;

import com.bank_management_system.shared.AccountNotFoundException;
import java.util.Arrays;

public class AccountManager {

    private static final int MAX_ACCOUNTS = 50;

    private final Account[] accounts = new Account[MAX_ACCOUNTS];
    private int accountCount = 0;

    /**
     * Adds a new account to the ledger.
     *
     * @param account the account to add
     * @throws IllegalStateException if the maximum account capacity has been reached
     */
    public void addAccount(Account account) {
        if (accountCount >= MAX_ACCOUNTS) {
            throw new IllegalStateException(
                    "Maximum account capacity of " + MAX_ACCOUNTS + " reached.");
        }
        accounts[accountCount++] = account;
    }

    /**
     * Finds and returns an account by its account number.
     *
     * @param accountNumber the account number to search for
     * @return the matching Account
     * @throws AccountNotFoundException if no account with the given number exists
     */
    public Account findAccountOrThrow(String accountNumber) {
        for (int i = 0; i < accountCount; i++) {
            if (accounts[i].getAccountNumber().equalsIgnoreCase(accountNumber)) {
                return accounts[i];
            }
        }
        throw new AccountNotFoundException("Account not found: " + accountNumber);
    }

    /**
     * Prints a formatted table of all accounts along with the combined bank balance.
     */
    public void viewAllAccounts() {
        if (accountCount == 0) {
            System.out.println("  No accounts found.");
            return;
        }

        System.out.println("  " + "=".repeat(76));
        System.out.printf("  %-8s | %-20s | %-8s | %12s | %s%n",
                "ACC NO", "CUSTOMER NAME", "TYPE", "BALANCE", "STATUS");
        System.out.println("  " + "=".repeat(76));

        for (int i = 0; i < accountCount; i++) {
            accounts[i].displayAccountDetails();
            System.out.println("  " + "-".repeat(76));
        }

        System.out.printf("  Total Accounts: %d | Total Bank Balance: $%,.2f%n",
                accountCount, getTotalBalance());
    }

    /**
     * Returns the sum of balances across all accounts.
     *
     * @return total balance held by the bank
     */
    public double getTotalBalance() {
        double total = 0;
        for (int i = 0; i < accountCount; i++) {
            total += accounts[i].getBalance();
        }
        return total;
    }

    /**
     * Returns a copy of all currently stored accounts.
     *
     * @return array of active accounts
     */
    public Account[] getAccounts() {
        return Arrays.copyOf(accounts, accountCount);
    }
}
