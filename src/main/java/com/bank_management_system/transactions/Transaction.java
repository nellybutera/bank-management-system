package com.bank_management_system.transactions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Transaction {

    private String transactionId;
    private String accountNumber;
    private String type;
    private double amount;
    private double balanceAfter;
    private String timestamp;

    private static int transactionCounter = 1;

    private static final DateTimeFormatter FORMATTED_DATE_TIME =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

    public Transaction(String accountNumber, String type, Double amount, double balanceAfter) {
        this.transactionId = String.format("TXN%03d", transactionCounter++);
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = LocalDateTime.now().format(FORMATTED_DATE_TIME);
    }

    /**
     * Returns the account number this transaction belongs to.
     *
     * @return the account number
     */
    public String getAccountNumber() { return accountNumber; }

    /**
     * Returns the transaction type (DEPOSIT or WITHDRAWAL).
     *
     * @return the transaction type
     */
    public String getType() { return type; }

    /**
     * Returns the transaction amount.
     *
     * @return the amount
     */
    public double getAmount() { return amount; }

    /**
     * Prints a formatted one-line summary of this transaction.
     * Example: TXN001 | 30-10-2025 10:30 AM | DEPOSIT | +$1,500.00 | $6,750.00
     */
    public void displayTransactionDetails() {
        String amountStr = type.equalsIgnoreCase("DEPOSIT")
                ? String.format("+$%,.2f", amount)
                : String.format("-$%,.2f", amount);

        System.out.printf("%-2S | %-4s | %-10S | %-12s | $%,.2f%n",
                transactionId,
                timestamp.toUpperCase(),
                type,
                amountStr,
                balanceAfter);
    }
}
