package com.bank_management_system.accounts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bank_management_system.customers.Customer;
import com.bank_management_system.shared.InsufficientFundsException;
import com.bank_management_system.shared.InvalidAmountException;
import com.bank_management_system.shared.Transactable;
import com.bank_management_system.transactions.Transaction;

public abstract class Account implements Transactable {
    private final String accountNumber;
    private final Customer customer;
    private double balance;
    private String status;
    private final List<Transaction> transactionHistory = new ArrayList<>();

    private static int accountCounter = 1;

    public Account(Customer customer, double balance){
        this.accountNumber = String.format("ACC%03d", accountCounter++);
        this.customer = customer;
        this.balance = balance;
        this.status = "Active";
    }

    public String getAccountNumber(){ return accountNumber; }
    public Customer getCustomer(){ return customer; }
    public String getCustomerName(){ return customer.getName();}
    public String getCustomerId(){ return customer.getCustomerId();}
    public double getBalance(){ return balance; }
    public String getStatus(){ return status;}
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactionHistory);
    }
    
    public abstract void displayAccountDetails();
    public abstract String getAccountType();
    protected abstract void validateWithdrawal(double amount) throws InsufficientFundsException;

    public final Transaction deposit(double amount){
        if ( amount <= 0){
            throw new InvalidAmountException("Deposit must be greater than zero");
        }
        balance += amount;
        Transaction transaction = new Transaction(accountNumber, "DEPOSIT", amount, balance);
        transactionHistory.add(transaction);
        return transaction;
    }

    public final Transaction withdraw(double amount) throws InvalidAmountException, InsufficientFundsException{

        if( amount <= 0){
            throw new InvalidAmountException("Withdrawal amount must be greater than zero.");
        }
        validateWithdrawal(amount);
        balance -= amount;

        Transaction transaction = new Transaction(accountNumber, "WITHDRAWAL", amount, balance);
        transactionHistory.add(transaction);
        return transaction;
    }

    @Override
    public boolean processTransaction(double amount, String type){
        try {
            if("deposit".equalsIgnoreCase(type)){
                deposit(amount);
            } else if ("withdrawal".equalsIgnoreCase(type)){
                withdraw(amount);
            } else {
                System.out.println("Invalid transaction type. Must be 'DEPOSIT' or 'WITHDRAWAL'.");
                return false;
            }

            return true;
        }catch(InvalidAmountException | InsufficientFundsException e){
            System.out.println("Transaction failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("[%s] %s Account | Owner: %s | Balance: $%,.2f | Status: %s",
                accountNumber, getAccountType(), customer.getName(), balance, status);
    }
    
}
