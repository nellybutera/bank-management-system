package com.bank_management_system.customers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bank_management_system.accounts.Account;

public abstract class Customer {
    private final String customerId;
    private String name;
    private int age;
    private String contact;
    private String address;
    private final List<Account> accounts;

    private static int customerCounter = 1;

    public Customer(String name, int age, String contact, String address){
        this.customerId = String.format("CUST%03d", customerCounter++);
        this.name = name;
        this.age = age;
        this.contact = contact;
        this.address = address;
        this.accounts = new ArrayList<>();
    }

    public boolean isEligibleForFeeWaiver(){
        return false;
    }

    //  getter methods
    public String getCustomerId(){ return customerId; }

    public String getName(){ return name; }
    public void setName(String fullname){
        name = fullname;
    }

    public int getAge(){ return age; }
    public void setAge(int userAge){
        age = userAge;
    }

    public String getContact(){ return contact; }
    public void setContact(String phone){
        contact = phone;
    }

    public String getAddress(){ return address; }
    public void setAddress(String location){
        address = location;
    }

    public void addAccount(Account account){
        accounts.add(account);
    }

    public List<Account> getAccounts(){
        return Collections.unmodifiableList(accounts);
    }
    
    // abstract methods
    public abstract void displayCustomerDetails();
    public abstract String getCustomerType();


    @Override
    public String toString() {
        return String.format("[%s] %s (%s) | Accounts: %d",
                customerId, name, getCustomerType(), accounts.size());
    }

    // additional methods coz i want to, and also a customer should be able to see the total value of their assets in the bank

    private double calculateTotalAssets() {
        double total = 0;
        for (Account acc : accounts) {
            total += acc.getBalance();
        }
        return total;
    }

    public void viewCustomerAccounts() {
        System.out.println("\n  --- ACCOUNT PORTFOLIO: " + name + " (" + customerId + ") ---");
        
        if (accounts.isEmpty()) {
            System.out.println("  No accounts found for this customer.");
            return;
        }

        // Table Header
        System.out.println("  " + "-".repeat(65));
        System.out.printf("  %-12s | %-18s | %10s | %-10s%n", 
                          "ACCOUNT NO", "TYPE", "BALANCE", "STATUS");
        System.out.println("  " + "-".repeat(65));

        // Loop through the dynamic ArrayList
        for (Account acc : accounts) {
            System.out.printf("  %-12s | %-18s | $%,10.2f | %-10s%n", 
                acc.getAccountNumber(), 
                acc.getAccountType(), 
                acc.getBalance(),
                acc.getStatus());
        }
        
        System.out.println("  " + "-".repeat(65));
        System.out.printf("  Total Accounts: %d | Combined Balance: $%,.2f%n", 
                          accounts.size(), calculateTotalAssets());
    }




}
