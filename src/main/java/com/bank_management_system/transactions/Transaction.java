package com.bank_management_system.transactions;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Transaction{
    // public enum Type{
    //     DEPOSIT,
    //     WITHDRAWAL
    // }

    private String transactionId;
    private String accountNumber;
    private String type;
    private double amount;
    private double balanceAfter;
    private String timestamp;

    private static int transactionCounter = 1;

    private static final DateTimeFormatter formatedDateTime = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

    public Transaction(String accountNumber, String type, Double amount, double balanceAfter){
        this.transactionId = String.format("TXN%03d", transactionCounter++);
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = LocalDateTime.now().format(formatedDateTime);
    }

    public String getAccountNumber(){ return accountNumber; }
    public String getType(){ return type; }
    public double getAmount(){ return amount; }

    /**
     * Displays a formatted one-line summary matching the task doc format:
     *   TXN001 | 30-10-2025 10:30 AM | DEPOSIT    | +$1,500.00 | $6,750.00
     */
    // The string format method in java follows this order: %[parameter_index$][flags][width][.precision], conversion_target
    // parameter index: is used to assign the order in which conversion targests are processed
    // flags can be used for multiple thing forexample adding ( will telling the string formatter to enclose negative numbers in parentheses, while - is making the output left justified.
    // width can be specific with a number usually on the left space will be created, but you can use the '-' flag to make space on the right instead
    public void displayTransactionDetails(){
        String amountStr = (type.equalsIgnoreCase("DEPOSIT"))
                ? String.format("+$%,.2f", amount)
                : String.format("-$%,.2f", amount);

        System.out.printf("%-2S | %-4s | %-10S | %-12s | $%,.2f%n", 
            transactionId,
            timestamp.toUpperCase(),
            type,
            amountStr,
            balanceAfter);
    };


}
