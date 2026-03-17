package com.bank_management_system.accounts;
import com.bank_management_system.shared.AccountNotFoundException;
import java.util.Arrays;


public class AccountManager {
    private static final int max_accounts = 50;

    private final Account[] accounts = new Account[max_accounts]; // Fixed-size array of accounts
    private int accountCount = 0;                                  // Tracks how many slots are used


    public void addAccount(Account account) {
        if (accountCount >= max_accounts) {
            throw new IllegalStateException(
                    "Maximum account capacity of " + max_accounts + " reached.");
        }
        accounts[accountCount++] = account;
    }

    public Account findAccountOrThrow(String accountNumber) {
        for (int i = 0; i < accountCount; i++) {
            if (accounts[i].getAccountNumber().equalsIgnoreCase(accountNumber)) {
                return accounts[i];
            }
        }
        throw new AccountNotFoundException("Account not found: " + accountNumber);
    }

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


    public double getTotalBalance() {
        double total = 0;
        for (int i = 0; i < accountCount; i++) {
            total += accounts[i].getBalance();
        }
        return total;
    }


    public Account[] getAccounts() {
        return Arrays.copyOf(accounts, accountCount);
    }
}