package com.bank_management_system.accounts;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.bank_management_system.exceptions.AccountNotFoundException;
import com.bank_management_system.utils.FunctionalUtils;

public class AccountManager {

    private final LinkedHashMap<String, Account> accounts = new LinkedHashMap<>();

    public void addAccount(Account account) {
        accounts.put(account.getAccountNumber().toUpperCase(), account);
    }

    public Account findAccountOrThrow(String accountNumber) {
        Account account = accounts.get(accountNumber.toUpperCase());
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
        return account;
    }

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

    public double getTotalBalance() {
        return FunctionalUtils.totalBalance(accounts.values());
    }

    public Collection<Account> getAccounts() {
        return accounts.values();
    }

    public List<Account> findAccountsMatching(Predicate<Account> criteria) {
        return accounts.values().stream()
                .filter(criteria)
                .collect(Collectors.toList());
    }
}
