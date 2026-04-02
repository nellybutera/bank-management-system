package com.bank_management_system.accounts;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.bank_management_system.exceptions.AccountNotFoundException;
import com.bank_management_system.utils.FunctionalUtils;

/**
 * In-memory store for all bank accounts.
 * Uses a {@link LinkedHashMap} to provide O(1) lookup by account number
 * while preserving insertion order for deterministic display.
 */
public class AccountManager {

    private final LinkedHashMap<String, Account> accounts = new LinkedHashMap<>();

    /**
     * Adds an account to the store, keyed by its account number (upper-cased).
     *
     * @param account the account to register
     */
    public void addAccount(Account account) {
        accounts.put(account.getAccountNumber().toUpperCase(), account);
    }

    /**
     * Returns the account for the given account number, or throws if not found.
     *
     * @param accountNumber the account number to look up (case-insensitive)
     * @return the matching Account
     * @throws com.bank_management_system.exceptions.AccountNotFoundException if no account exists with that number
     */
    public Account findAccountOrThrow(String accountNumber) {
        Account account = accounts.get(accountNumber.toUpperCase());
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
        return account;
    }

    /**
     * Prints a formatted table of all accounts to the console,
     * followed by the total account count and combined bank balance.
     */
    public void viewAllAccounts() {
        if (accounts.isEmpty()) {
            System.out.println("  No accounts found.");
            return;
        }

        System.out.println("  " + "=".repeat(76));
        System.out.printf("  %-8s | %-20s | %-8s | %12s | %s%n",
                "ACC NO", "CUSTOMER NAME", "TYPE", "BALANCE", "STATUS");
        System.out.println("  " + "=".repeat(76));

        accounts.values().forEach(account -> {
            account.displayAccountDetails();
            System.out.println("  " + "-".repeat(76));
        });

        System.out.printf("  Total Accounts: %d | Total Bank Balance: $%,.2f%n",
                accounts.size(), getTotalBalance());
    }

    /** Returns the sum of current balances across all accounts. */
    public double getTotalBalance() {
        return FunctionalUtils.totalBalance(accounts.values());
    }

    /** Returns a live view of all accounts (insertion order preserved). */
    public Collection<Account> getAccounts() {
        return accounts.values();
    }

    /**
     * Returns all accounts that satisfy the given predicate.
     * Used by {@link com.bank_management_system.accounts.AccountService} to apply fees and interest
     * to a filtered subset of accounts without exposing the internal map.
     *
     * @param criteria a {@link Predicate} that returns true for accounts to include
     * @return a new list containing only the matching accounts
     */
    public List<Account> findAccountsMatching(Predicate<Account> criteria) {
        return accounts.values().stream()
                .filter(criteria)
                .collect(Collectors.toList());
    }
}
