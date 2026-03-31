package com.bank_management_system.customers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bank_management_system.accounts.Account;
import com.bank_management_system.utils.FunctionalUtils;

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

    /** Restoration constructor — rebuilds a Customer from file without touching the counter. */
    protected Customer(String customerId, String name, int age, String contact, String address) {
        this.customerId = customerId;
        this.name = name;
        this.age = age;
        this.contact = contact;
        this.address = address;
        this.accounts = new ArrayList<>();
    }

    /** Sets the customer ID counter so the next new customer gets the correct ID after loading from file. */
    public static void resetCounter(int value) {
        customerCounter = value;
    }

    /** Returns true if this customer qualifies for a monthly fee waiver. Default is false. */
    public boolean isEligibleForFeeWaiver() {
        return false;
    }

    /** Returns the unique customer ID (e.g. CUST001). */
    public String getCustomerId() { return customerId; }

    /** Returns the customer's full name. */
    public String getName() { return name; }

    /** Updates the customer's full name. */
    public void setName(String fullname) { name = fullname; }

    /** Returns the customer's age. */
    public int getAge() { return age; }

    /** Updates the customer's age. */
    public void setAge(int userAge) { age = userAge; }

    /** Returns the customer's contact number. */
    public String getContact() { return contact; }

    /** Updates the customer's contact number. */
    public void setContact(String phone) { contact = phone; }

    /** Returns the customer's address. */
    public String getAddress() { return address; }

    /** Updates the customer's address. */
    public void setAddress(String location) { address = location; }

    /** Links an account to this customer's portfolio. */
    public void addAccount(Account account) {
        accounts.add(account);
    }

    /** Returns an unmodifiable view of this customer's accounts. */
    public List<Account> getAccounts() {
        return Collections.unmodifiableList(accounts);
    }

    /** Displays this customer's profile details. */
    public abstract void displayCustomerDetails();

    /** Returns the customer tier label (e.g. "Regular" or "Premium"). */
    public abstract String getCustomerType();


    @Override
    public String toString() {
        return String.format("[%s] %s (%s) | Accounts: %d",
                customerId, name, getCustomerType(), accounts.size());
    }

    private double calculateTotalAssets() {
        return FunctionalUtils.totalBalance(accounts);
    }

    /**
     * Prints a formatted table of all accounts in this customer's portfolio,
     * along with the total number of accounts and combined balance.
     */
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
