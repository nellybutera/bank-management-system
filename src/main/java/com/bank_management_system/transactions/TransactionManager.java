package com.bank_management_system.transactions;

public class TransactionManager {

    private static final int MAX_TRANSACTIONS = 200;

    private final Transaction[] transactions = new Transaction[MAX_TRANSACTIONS];
    private int transactionCount = 0;

    /**
     * Records a transaction in the ledger.
     * Prints a warning if the ledger is full and the transaction cannot be stored.
     *
     * @param transaction the transaction to record
     */
    public void addTransaction(Transaction transaction) {
        if (transactionCount < MAX_TRANSACTIONS) {
            transactions[transactionCount++] = transaction;
        } else {
            System.out.println("Warning: Transaction history is full. Transaction not recorded.");
        }
    }

    /**
     * Displays the full transaction history for a given account, newest first,
     * followed by a summary of totals.
     *
     * @param accountNumber the account number to look up
     */
    public void viewTransactionsByAccount(String accountNumber) {
        int matchCount = countMatchingTransactions(accountNumber);

        if (matchCount == 0) {
            System.out.println("  No transactions found for account: " + accountNumber);
            return;
        }

        printTransactionTable(accountNumber);
        printTransactionSummary(accountNumber, matchCount);
    }

    /**
     * Returns the total amount deposited into the given account.
     *
     * @param accountNumber the account number to query
     * @return total deposits
     */
    public double calculateTotalDeposits(String accountNumber) {
        return sumByTransactionType(accountNumber, "DEPOSIT");
    }

    /**
     * Returns the total amount withdrawn from the given account.
     *
     * @param accountNumber the account number to query
     * @return total withdrawals
     */
    public double calculateTotalWithdrawals(String accountNumber) {
        return sumByTransactionType(accountNumber, "WITHDRAWAL");
    }

    /** Counts how many recorded transactions belong to the given account. */
    private int countMatchingTransactions(String accountNumber) {
        int count = 0;
        for (int i = 0; i < transactionCount; i++) {
            if (transactions[i].getAccountNumber().equalsIgnoreCase(accountNumber)) {
                count++;
            }
        }
        return count;
    }

    /** Prints the header and rows of the transaction table, newest entry first. */
    private void printTransactionTable(String accountNumber) {
        System.out.println("  TRANSACTION HISTORY");
        System.out.println("  " + "-".repeat(80));
        System.out.printf("  %-7s | %-20s | %-11s | %12s | %s%n",
                "TXN ID", "DATE/TIME", "TYPE", "AMOUNT", "BALANCE");
        System.out.println("  " + "-".repeat(80));

        for (int i = transactionCount - 1; i >= 0; i--) {
            if (transactions[i].getAccountNumber().equalsIgnoreCase(accountNumber)) {
                transactions[i].displayTransactionDetails();
            }
        }
    }

    /** Prints the deposit, withdrawal, and net change totals for the given account. */
    private void printTransactionSummary(String accountNumber, int matchCount) {
        double totalDeposits    = calculateTotalDeposits(accountNumber);
        double totalWithdrawals = calculateTotalWithdrawals(accountNumber);

        System.out.println("  " + "-".repeat(80));
        System.out.printf("  Total Transactions : %d%n",      matchCount);
        System.out.printf("  Total Deposits     : $%,.2f%n",  totalDeposits);
        System.out.printf("  Total Withdrawals  : $%,.2f%n",  totalWithdrawals);
        System.out.printf("  Net Change         : +$%,.2f%n", totalDeposits - totalWithdrawals);
    }

    /** Sums all transaction amounts of a given type for the specified account. */
    private double sumByTransactionType(String accountNumber, String type) {
        double total = 0;
        for (int i = 0; i < transactionCount; i++) {
            if (transactions[i].getAccountNumber().equalsIgnoreCase(accountNumber)
                    && transactions[i].getType().equalsIgnoreCase(type)) {
                total += transactions[i].getAmount();
            }
        }
        return total;
    }
}
