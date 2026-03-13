package com.bank_management_system.transactions;

public class TransactionManager {

    private static final int max_transactions = 200;

    private final Transaction[] transactions = new Transaction[max_transactions];
    private int transactionCount = 0;

    public void addTransaction(Transaction transaction){
        if( transactionCount < max_transactions){
            transactions[transactionCount++] = transaction;
        } else {
            System.out.println("Warning:Transaction history is full. Transaction not recorded.");
        }
    }

    public void viewTransactionsByAccount(String accountNumber) {
        // Count matching transactions first
        int count = 0;
        for (int i = 0; i < transactionCount; i++) {
            if (transactions[i].getAccountNumber().equalsIgnoreCase(accountNumber)) count++;
        }

        if (count == 0) {
            System.out.println("  No transactions found for account: " + accountNumber);
            return;
        }

        // Column header
        System.out.println("  TRANSACTION HISTORY");
        System.out.println("  " + "-".repeat(80));
        System.out.printf("  %-7s | %-20s | %-11s | %12s | %s%n",
                "TXN ID", "DATE/TIME", "TYPE", "AMOUNT", "BALANCE");
        System.out.println("  " + "-".repeat(80));

        // newest first
        for (int i = transactionCount - 1; i >= 0; i--) { // this backwards loop ensures newest transactions are shown first
            if (transactions[i].getAccountNumber().equalsIgnoreCase(accountNumber)) {
                transactions[i].displayTransactionDetails();
            }
        }

        // Summary
        double totalDeposits    = calculateTotalDeposits(accountNumber);
        double totalWithdrawals = calculateTotalWithdrawals(accountNumber);
        System.out.println("  " + "-".repeat(80));
        System.out.printf("  Total Transactions : %d%n",       count);
        System.out.printf("  Total Deposits     : $%,.2f%n",   totalDeposits);
        System.out.printf("  Total Withdrawals  : $%,.2f%n",   totalWithdrawals);
        System.out.printf("  Net Change         : +$%,.2f%n",  totalDeposits - totalWithdrawals);
    }
    
    public double calculateTotalDeposits(String accountNumber) {
        double total = 0;
        for (int i = 0; i < transactionCount; i++) {
            if (transactions[i].getAccountNumber().equalsIgnoreCase(accountNumber)
                    && transactions[i].getType().equalsIgnoreCase("DEPOSIT")) {
                total += transactions[i].getAmount();
            }
        }
        return total;
    }

    public double calculateTotalWithdrawals(String accountNumber) {
        double total = 0;
        for (int i = 0; i < transactionCount; i++) {
            if (transactions[i].getAccountNumber().equalsIgnoreCase(accountNumber)
                    && transactions[i].getType().equalsIgnoreCase("WITHDRAWAL")) {
                total += transactions[i].getAmount();
            }
        }
        return total;
    }

    public int getTransactionCount() {
        return transactionCount;
    }
}
