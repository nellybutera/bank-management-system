package com.bank_management_system.accounts;

import com.bank_management_system.customers.Customer;

public abstract class Account {
    private final String accountNumber;
    private final Customer customer;
    private double balance;
    private String status;

    private static int accountCounter = 1;

    public Account(String accountNumber, Customer customer, double balance, String status){
        this.accountNumber = String.format("ACC%03d", accountCounter++);
        this.customer = customer;
        this.balance = balance;
        this.status = status;
    }

    public String getAccountNumber(){ return accountNumber; }
    public Customer getCustomer(){ return customer; }
    public String getCustomerName(){ return customer.getName();}
    public String getCustomerId(){ return customer.getCustomerId();}
    public double getBalance(){ return balance; }
    public String getStatus(){ return status;}
    
    public abstract void displayAccountDetails();
    public abstract String getAccountType();

    public void deposit(double amount){
        balance += amount;
    }

    public void withdraw(double amount){
        balance = balance - amount;
    }
    
}
