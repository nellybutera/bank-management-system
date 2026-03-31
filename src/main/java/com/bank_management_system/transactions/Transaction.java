package com.bank_management_system.transactions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Transaction {

    private String transactionId;
    private String accountNumber;
    private String type;
    private double amount;
    private double balanceAfter;
    private LocalDateTime createdAt;
    private String timestamp;

    private static int transactionCounter = 1;

    private static final DateTimeFormatter FORMATTED_DATE_TIME =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a", Locale.ENGLISH);

    public Transaction(String accountNumber, String type, Double amount, double balanceAfter) {
        this.transactionId = String.format("TXN%03d", transactionCounter++);
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.createdAt = LocalDateTime.now();
        this.timestamp = createdAt.format(FORMATTED_DATE_TIME);
    }

    /** Restoration constructor — rebuilds a Transaction from file without auto-generating an ID. */
    private Transaction(String transactionId, String accountNumber, String type,
                        double amount, double balanceAfter, String timestamp) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.type          = type;
        this.amount        = amount;
        this.balanceAfter  = balanceAfter;
        this.timestamp     = timestamp;
        this.createdAt     = LocalDateTime.parse(timestamp, FORMATTED_DATE_TIME);
    }

    /**
     * Rebuilds a Transaction from a pipe-delimited file line.
     * Format: transactionId|accountNumber|type|amount|balanceAfter|timestamp
     * Used as a method reference: {@code Transaction::fromLine}
     */
    public static Transaction fromLine(String line) {
        String[] p = line.split("\\|");
        return new Transaction(p[0], p[1], p[2],
                Double.parseDouble(p[3]), Double.parseDouble(p[4]), p[5]);
    }

    /** Serializes this transaction to a pipe-delimited line for file storage. */
    public String toFileLine() {
        return String.join("|", transactionId, accountNumber, type,
                String.valueOf(amount), String.valueOf(balanceAfter), timestamp);
    }

    /** Returns the transaction ID (e.g. TXN001). */
    public String getTransactionId() { return transactionId; }

    /** Sets the transaction ID counter so the next new transaction gets the correct ID after loading from file. */
    public static void resetCounter(int value) {
        transactionCounter = value;
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
     * Returns the timestamp as a LocalDateTime, used for sorting.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * Prints a formatted one-line summary of this transaction.
     * Example: TXN001 | 30-10-2025 10:30 AM | DEPOSIT | +$1,500.00 | $6,750.00
     */
    public void displayTransactionDetails() {
        String amountStr = type.equalsIgnoreCase("DEPOSIT")
                ? String.format("+$%,.2f", amount)
                : String.format("-$%,.2f", amount);

        System.out.printf("  %-7s | %-20s | %-11s | %12s | $%,.2f%n",
                transactionId,
                timestamp,
                type,
                amountStr,
                balanceAfter);
    }
}
