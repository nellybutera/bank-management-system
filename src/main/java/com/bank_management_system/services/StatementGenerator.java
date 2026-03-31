package com.bank_management_system.services;

import com.bank_management_system.accounts.Account;
import com.bank_management_system.transactions.Transaction;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates a formatted account statement for a single account.
 *
 * Responsibilities:
 * - Sort transactions by timestamp (newest first)
 * - Display a clean header with account details and current balance
 * - Handle accounts with no transactions gracefully
 * - Print deposit/withdrawal totals and net change with 2-decimal precision
 * - Confirm success with a checkmark message
 */
public class StatementGenerator {

    private StatementGenerator() {}

    /**
     * Prints a formatted account statement to the console.
     *
     * @param account      the account to generate the statement for
     * @param transactions all transactions in the ledger (unfiltered — the generator handles filtering)
     */
    public static void generate(Account account, List<Transaction> transactions) {
        List<Transaction> accountTransactions = transactions.stream()
                .filter(t -> t.getAccountNumber().equalsIgnoreCase(account.getAccountNumber()))
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .collect(Collectors.toList());

        printHeader(account);

        if (accountTransactions.isEmpty()) {
            System.out.println("  No transactions on record for this account.");
        } else {
            printTransactions(accountTransactions);
            printSummary(accountTransactions);
        }

        System.out.println("=".repeat(70));
        System.out.println("\u2713 Statement generated successfully.");
    }

    private static void printHeader(Account account) {
        System.out.println("=".repeat(70));
        System.out.println("  ACCOUNT STATEMENT");
        System.out.println("=".repeat(70));
        System.out.printf("  Account Number : %s%n",       account.getAccountNumber());
        System.out.printf("  Account Holder : %s%n",       account.getCustomerName());
        System.out.printf("  Account Type   : %s%n",       account.getAccountType());
        System.out.printf("  Status         : %s%n",       account.getStatus());
        System.out.printf("  Current Balance: $%,.2f%n",   account.getBalance());
        System.out.println("-".repeat(70));
        System.out.println("  TRANSACTIONS (newest first)");
        System.out.println("-".repeat(70));
        System.out.printf("  %-7s | %-20s | %-12s | %12s | %s%n",
                "TXN ID", "DATE/TIME", "TYPE", "AMOUNT", "BALANCE");
        System.out.println("-".repeat(70));
    }

    private static void printTransactions(List<Transaction> transactions) {
        transactions.forEach(Transaction::displayTransactionDetails);
    }

    private static void printSummary(List<Transaction> transactions) {
        double totalDeposits    = sumByType(transactions, "DEPOSIT");
        double totalWithdrawals = sumByType(transactions, "WITHDRAWAL");
        double totalTransferIn  = sumByType(transactions, "TRANSFER_IN");
        double totalTransferOut = sumByType(transactions, "TRANSFER_OUT");

        double totalCredits = totalDeposits + totalTransferIn;
        double totalDebits  = totalWithdrawals + totalTransferOut;
        double netChange    = totalCredits - totalDebits;
        String netPrefix    = netChange >= 0 ? "+$" : "-$";

        System.out.println("-".repeat(70));
        System.out.printf("  Total Deposits     : $%,.2f%n", totalDeposits);
        System.out.printf("  Total Withdrawals  : $%,.2f%n", totalWithdrawals);
        if (totalTransferIn  > 0) System.out.printf("  Transfers In       : $%,.2f%n", totalTransferIn);
        if (totalTransferOut > 0) System.out.printf("  Transfers Out      : $%,.2f%n", totalTransferOut);
        System.out.printf("  Net Change         : %s%,.2f%n", netPrefix, Math.abs(netChange));
    }

    private static double sumByType(List<Transaction> transactions, String type) {
        return transactions.stream()
                .filter(t -> t.getType().equalsIgnoreCase(type))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
}
