package com.bank_management_system.utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.bank_management_system.accounts.Account;
import com.bank_management_system.transactions.Transaction;

/**
 * Reusable stream and lambda operations shared across the banking system.
 * All methods are stateless and work on the data passed to them.
 */
public class FunctionalUtils {

    private FunctionalUtils() {}

    /**
     * Groups a list of transactions by their type (e.g. "DEPOSIT", "WITHDRAWAL").
     * Useful for building summaries without scanning the list multiple times.
     */
    public static Map<String, List<Transaction>> groupByType(List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getType().toUpperCase()));
    }

    /**
     * Sums the amounts in a list of transactions.
     * Intended to be used on a group returned by {@link #groupByType}.
     */
    public static double sumAmounts(List<Transaction> transactions) {
        return transactions.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /**
     * Returns the transaction with the highest amount, or empty if the list is empty.
     */
    public static Optional<Transaction> largestTransaction(List<Transaction> transactions) {
        return transactions.stream()
                .max(Comparator.comparingDouble(Transaction::getAmount));
    }

    /**
     * Sums the current balances across a collection of accounts.
     */
    public static double totalBalance(Collection<Account> accounts) {
        return accounts.stream()
                .mapToDouble(Account::getBalance)
                .sum();
    }
}
