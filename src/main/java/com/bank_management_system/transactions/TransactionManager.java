package com.bank_management_system.transactions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.bank_management_system.utils.FunctionalUtils;

public class TransactionManager {

    private final List<Transaction> transactions = Collections.synchronizedList(new ArrayList<>());

    public synchronized void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public List<Transaction> getAllTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public void viewTransactionsByAccount(String accountNumber, Comparator<Transaction> sortOrder) {
        List<Transaction> accountTransactions = getTransactionsForAccount(accountNumber);

        if (accountTransactions.isEmpty()) {
            System.out.println("  No transactions found for account: " + accountNumber);
            return;
        }

        printTransactionTable(accountTransactions, sortOrder);
        printTransactionSummary(accountTransactions);
    }

    public double calculateTotalDeposits(String accountNumber) {
        return sumByType(accountNumber, "DEPOSIT");
    }

    public double calculateTotalWithdrawals(String accountNumber) {
        return sumByType(accountNumber, "WITHDRAWAL");
    }

    private List<Transaction> getTransactionsForAccount(String accountNumber) {
        return transactions.stream()
                .filter(t -> t.getAccountNumber().equalsIgnoreCase(accountNumber))
                .collect(Collectors.toList());
    }

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

    private double sumByType(String accountNumber, String type) {
        return transactions.stream()
                .filter(t -> t.getAccountNumber().equalsIgnoreCase(accountNumber))
                .filter(t -> t.getType().equalsIgnoreCase(type))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
}
