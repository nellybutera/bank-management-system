package com.bank_management_system.transactions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.bank_management_system.utils.FunctionalUtils;

/**
 * In-memory ledger that stores and queries all transactions across the system.
 *
 * Thread safety: the backing list is a {@code Collections.synchronizedList} and
 * {@link #addTransaction} is additionally {@code synchronized} to guarantee that
 * no two threads can interleave a read-then-write on the list simultaneously.
 */
public class TransactionManager {

    private final List<Transaction> transactions = Collections.synchronizedList(new ArrayList<>());

    /**
     * Adds a transaction to the ledger. Synchronized to prevent concurrent
     * modifications from interleaving with list resizes.
     *
     * @param transaction the transaction to record
     */
    public synchronized void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    /** Returns an unmodifiable snapshot of all recorded transactions. */
    public List<Transaction> getAllTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    /**
     * Prints a formatted transaction history table for the given account,
     * sorted by the provided comparator, followed by a summary.
     *
     * @param accountNumber the account to filter by
     * @param sortOrder     the comparator that controls display order
     */
    public void viewTransactionsByAccount(String accountNumber, Comparator<Transaction> sortOrder) {
        List<Transaction> accountTransactions = getTransactionsForAccount(accountNumber);

        if (accountTransactions.isEmpty()) {
            System.out.println("  No transactions found for account: " + accountNumber);
            return;
        }

        printTransactionTable(accountTransactions, sortOrder);
        printTransactionSummary(accountTransactions);
    }

    /**
     * Returns the total amount deposited into the specified account.
     *
     * @param accountNumber the account to aggregate deposits for
     * @return the sum of all DEPOSIT transaction amounts
     */
    public double calculateTotalDeposits(String accountNumber) {
        return sumByType(accountNumber, "DEPOSIT");
    }

    /**
     * Returns the total amount withdrawn from the specified account.
     *
     * @param accountNumber the account to aggregate withdrawals for
     * @return the sum of all WITHDRAWAL transaction amounts
     */
    public double calculateTotalWithdrawals(String accountNumber) {
        return sumByType(accountNumber, "WITHDRAWAL");
    }

    /** Filters all transactions down to only those belonging to the given account. */
    private List<Transaction> getTransactionsForAccount(String accountNumber) {
        return transactions.stream()
                .filter(t -> t.getAccountNumber().equalsIgnoreCase(accountNumber))
                .collect(Collectors.toList());
    }

    /** Prints the header row and sorted transaction rows for the given list. */
    private void printTransactionTable(List<Transaction> accountTransactions, Comparator<Transaction> sortOrder) {
        System.out.println("  TRANSACTION HISTORY");
        System.out.println("  " + "-".repeat(80));
        System.out.printf("  %-7s | %-20s | %-11s | %12s | %s%n",
                "TXN ID", "DATE/TIME", "TYPE", "AMOUNT", "BALANCE");
        System.out.println("  " + "-".repeat(80));

        accountTransactions.stream()
                .sorted(sortOrder)
                .forEach(Transaction::displayTransactionDetails);
    }

    /** Prints deposit/withdrawal/transfer totals and net change for the given transaction list. */
    private void printTransactionSummary(List<Transaction> accountTransactions) {
        Map<String, List<Transaction>> byType = FunctionalUtils.groupByType(accountTransactions);
        double totalDeposits      = FunctionalUtils.sumAmounts(byType.getOrDefault("DEPOSIT",      List.of()));
        double totalWithdrawals   = FunctionalUtils.sumAmounts(byType.getOrDefault("WITHDRAWAL",   List.of()));
        double totalTransferIn    = FunctionalUtils.sumAmounts(byType.getOrDefault("TRANSFER_IN",  List.of()));
        double totalTransferOut   = FunctionalUtils.sumAmounts(byType.getOrDefault("TRANSFER_OUT", List.of()));

        double totalCredits = totalDeposits + totalTransferIn;
        double totalDebits  = totalWithdrawals + totalTransferOut;
        double netChange    = totalCredits - totalDebits;
        String netPrefix    = netChange >= 0 ? "+$" : "-$";

        System.out.println("  " + "-".repeat(80));
        System.out.printf("  Total Transactions : %d%n",      accountTransactions.size());
        System.out.printf("  Total Deposits     : $%,.2f%n",  totalDeposits);
        System.out.printf("  Total Withdrawals  : $%,.2f%n",  totalWithdrawals);
        if (totalTransferIn  > 0) System.out.printf("  Transfers In       : $%,.2f%n", totalTransferIn);
        if (totalTransferOut > 0) System.out.printf("  Transfers Out      : $%,.2f%n", totalTransferOut);
        System.out.printf("  Net Change         : %s%,.2f%n", netPrefix, Math.abs(netChange));

        FunctionalUtils.largestTransaction(accountTransactions).ifPresent(t ->
                System.out.printf("  Largest Transaction: $%,.2f (%s)%n", t.getAmount(), t.getType()));
    }

    /** Sums transaction amounts for a specific account and transaction type. */
    private double sumByType(String accountNumber, String type) {
        return transactions.stream()
                .filter(t -> t.getAccountNumber().equalsIgnoreCase(accountNumber))
                .filter(t -> t.getType().equalsIgnoreCase(type))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
}
