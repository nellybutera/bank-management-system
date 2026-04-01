package com.bank_management_system.transactions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Immutable record of a single financial event on an account.
 * All fields are {@code final} — a Transaction cannot be altered after creation,
 * making it safe to share across threads and to pass into streams without defensive copying.
 *
 * <p>Supported types: {@code DEPOSIT}, {@code WITHDRAWAL}, {@code TRANSFER_IN}, {@code TRANSFER_OUT}.</p>
 */
public final class Transaction {

    private final String transactionId;
    private final String accountNumber;
    private final String type;
    private final double amount;
    private final double balanceAfter;
    private final LocalDateTime createdAt;
    private final String timestamp;

    private static int transactionCounter = 1;

    private static final DateTimeFormatter FORMATTED_DATE_TIME =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a", Locale.ENGLISH);

    /**
     * Creates a new transaction and auto-assigns the next sequential ID.
     *
     * @param accountNumber the account this transaction belongs to
     * @param type          the transaction type (DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT)
     * @param amount        the transaction amount (must be positive)
     * @param balanceAfter  the account balance immediately after this transaction
     */
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
        boolean isCredit = type.equalsIgnoreCase("DEPOSIT") || type.equalsIgnoreCase("TRANSFER_IN");
        String amountStr = isCredit
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
