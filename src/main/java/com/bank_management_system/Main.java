package com.bank_management_system;

import com.bank_management_system.accounts.Account;
import com.bank_management_system.accounts.AccountManager;
import com.bank_management_system.accounts.AccountService;
import com.bank_management_system.accounts.CheckingAccount;
import com.bank_management_system.accounts.SavingsAccount;
import com.bank_management_system.bank.Bank;
import com.bank_management_system.customers.Customer;
import com.bank_management_system.customers.CustomerService;
import com.bank_management_system.customers.RegularCustomer;
import com.bank_management_system.shared.IllegalArgumentException;
import com.bank_management_system.shared.InputValidator;
import com.bank_management_system.transactions.TransactionManager;

import java.util.Scanner;

public class Main {
    private static final Bank bank = new Bank();
    private static final AccountManager accountManager = new AccountManager();
    private static final TransactionManager transactionManager = new TransactionManager();
    private static final AccountService accountService = new AccountService(bank, accountManager, transactionManager);
    private static final CustomerService customerService = new CustomerService(bank);

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        initializeSampleData();
        printWelcome();

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readMenuChoice(1, 5);

            switch (choice) {
                case 1 -> handleCreateAccount();
                case 2 -> handleViewAccounts();
                case 3 -> handleProcessTransaction();
                case 4 -> handleViewTransactionHistory();
                case 5 -> running = false;
            }
        }

        System.out.println("\nThank you for using Bank Account Management System!");
        System.out.println("Goodbye!");
        scanner.close();
    }

    // sample data
    // sample data

    private static void initializeSampleData() {
        try {
            // customerService.register already adds each customer to the bank
            RegularCustomer c1 = customerService.registerRegularCustomer("John Smith",    35, "555-1234", "123 Main St, Springfield");
            RegularCustomer c2 = customerService.registerRegularCustomer("Sarah Johnson", 28, "555-2345", "456 Oak Ave, Riverside");
            RegularCustomer c3 = customerService.registerRegularCustomer("Michael Chen",  42, "555-3456", "789 Pine Rd, Lakewood");
            RegularCustomer c4 = customerService.registerRegularCustomer("Emily Brown",   31, "555-4567", "321 Elm St, Hillside");
            RegularCustomer c5 = customerService.registerRegularCustomer("David Wilson",  55, "555-5678", "654 Maple Dr, Westfield");

            accountService.createSavingsAccount(c1.getCustomerId(),  5_250.00);
            accountService.createCheckingAccount(c2.getCustomerId(), 3_450.00);
            accountService.createSavingsAccount(c3.getCustomerId(), 15_750.00);
            accountService.createCheckingAccount(c4.getCustomerId(),   890.00);
            accountService.createSavingsAccount(c5.getCustomerId(), 25_300.00);

        } catch (Exception e) {
            System.err.println("Error loading sample data: " + e.getMessage());
        }
    }

    // menu handlers
    // menu handlers

    private static void handleViewAccounts() {
        System.out.println("\n--- ALL BANK ACCOUNTS ---");
        accountService.displayAllAccounts();
        pressEnterToContinue();
    }

    private static void handleProcessTransaction() {
        System.out.println("\n--- PROCESS TRANSACTION ---");
        System.out.print("Enter Account Number: ");
        String accNum = scanner.nextLine().trim();

        try {
            Account account = accountService.getAccountDetails(accNum);

            // Show account details
            System.out.println("\nAccount Details:");
            System.out.printf("Customer: %s | Account Type: %s | Current Balance: $%,.2f%n",
                    account.getCustomerName(), account.getAccountType(), account.getBalance());

            // Transaction type
            System.out.println("\nTransaction type:");
            System.out.println("  1. Deposit");
            System.out.println("  2. Withdrawal");
            System.out.print("Select type (1-2): ");
            int type = readMenuChoice(1, 2);

            System.out.print("Enter amount: $");
            double amount = readAmount();

            // Transaction preview
            double previousBalance = account.getBalance();
            double expectedBalance = (type == 1) ? previousBalance + amount : previousBalance - amount;
            String txnType = (type == 1) ? "DEPOSIT" : "WITHDRAWAL";

            System.out.println("\nTRANSACTION CONFIRMATION");
            System.out.println("-".repeat(50));
            System.out.printf("Account: %s | Type: %s%n", accNum, txnType);
            System.out.printf("Amount: $%,.2f%n", amount);
            System.out.printf("Previous Balance: $%,.2f | New Balance: $%,.2f%n", previousBalance, expectedBalance);
            System.out.print("\nConfirm transaction? (Y/N): ");
            String confirm = scanner.nextLine().trim();

            if (!confirm.equalsIgnoreCase("Y")) {
                System.out.println("Transaction cancelled.");
                pressEnterToContinue();
                return;
            }

            // Execute
            if (type == 1) {
                accountService.deposit(accNum, amount);
            } else {
                accountService.withdraw(accNum, amount);
            }

            System.out.println("\nTransaction completed successfully!");

        } catch (Exception e) {
            printError(e.getMessage());
        }
        pressEnterToContinue();
    }

    private static void handleViewTransactionHistory() {
        System.out.println("\n--- VIEW TRANSACTION HISTORY ---");
        System.out.print("Enter Account Number: ");
        String accNum = scanner.nextLine().trim();

        try {
            Account account = accountService.getAccountDetails(accNum);

            // Account header
            System.out.println("\nAccount: " + accNum + " - " + account.getCustomerName());
            System.out.println("Account Type: " + account.getAccountType());
            System.out.printf("Current Balance: $%,.2f%n", account.getBalance());
            System.out.println();

            accountService.getTransactionHistory(accNum);

        } catch (Exception e) {
            printError(e.getMessage());
        }
        pressEnterToContinue();
    }

    private static void handleCreateAccount() {
        System.out.println("\n--- ACCOUNT CREATION ---");

        // Collect customer details
        System.out.print("Enter customer name    : ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter customer age     : ");
        int age = readPositiveInt("age");

        System.out.print("Enter customer contact : ");
        String contact = scanner.nextLine().trim();

        System.out.print("Enter customer address : ");
        String address = scanner.nextLine().trim();

        // Customer type
        System.out.println("\nCustomer type:");
        System.out.println("  1. Regular Customer (Standard banking services)");
        System.out.println("  2. Premium Customer (Enhanced benefits, min balance $10,000)");
        System.out.print("Select type (1-2): ");
        int customerType = readMenuChoice(1, 2);

        // Account type
        System.out.println("\nAccount type:");
        System.out.printf("  1. Savings Account  (Interest: %.1f%%, Min Balance: $%.2f)%n", 3.5, 500.00);
        System.out.printf("  2. Checking Account (Overdraft: $%,.2f, Monthly Fee: $%.2f)%n", 1_000.00, 10.00);
        System.out.print("Select type (1-2): ");
        int accountType = readMenuChoice(1, 2);

        System.out.print("Enter initial deposit amount: $");
        double initialBalance = readAmount();

        try {
            // Register customer
            Customer customer;
            if (customerType == 1) {
                customer = customerService.registerRegularCustomer(name, age, contact, address);
            } else {
                customer = customerService.registerPremiumCustomer(name, age, contact, address);
            }

            // Create account
            Account account;
            if (accountType == 1) {
                account = accountService.createSavingsAccount(customer.getCustomerId(), initialBalance);
            } else {
                account = accountService.createCheckingAccount(customer.getCustomerId(), initialBalance);
            }

            // Confirmation
            System.out.println("\nAccount created successfully!");
            System.out.println("=".repeat(60));
            System.out.println("Account Number: " + account.getAccountNumber());
            System.out.printf("Customer: %s (%s) | Account Type: %s%n",
                    customer.getName(), customer.getCustomerType(), account.getAccountType());
            System.out.printf("Initial Balance: $%,.2f%n", account.getBalance());

            if (account instanceof SavingsAccount sa) {
                System.out.printf("Interest Rate: %.1f%% | Minimum Balance: $%.2f%n",
                        sa.getInterestRate() * 100, SavingsAccount.getMinimumBalance());
            } else if (account instanceof CheckingAccount ca) {
                System.out.printf("Overdraft Limit: $1,000.00 | Monthly Fee: %s%n",
                        ca.isFeesWaived() ? "$0.00 (WAIVED - Premium Customer)" : "$10.00");
            }

            System.out.println("Status: " + account.getStatus());
            System.out.println("=".repeat(60));

        } catch (Exception e) {
            printError(e.getMessage());
        }
        pressEnterToContinue();
    }

    // ui methods
    // ui methods

    private static void printWelcome() {
        System.out.println("==============================================");
        System.out.println("|   BANK ACCOUNT MANAGEMENT SYSTEM           |");
        System.out.println("==============================================");
    }

    private static void printMenu() {
        System.out.println("\n----------------------------------------------");
        System.out.println("|         BANK ACCOUNT MANAGEMENT            |");
        System.out.println("|              MAIN MENU                     |");
        System.out.println("----------------------------------------------");
        System.out.println("  1. Create Account");
        System.out.println("  2. View Accounts");
        System.out.println("  3. Process Transaction");
        System.out.println("  4. View Transaction History");
        System.out.println("  5. Exit");
        System.out.println("----------------------------------------------");
        System.out.print("Enter choice: ");
    }

    // Reads and validates a menu choice in [min, max]. Loops until valid.
    private static int readMenuChoice(int min, int max) {
        while (true) {
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                InputValidator.validateMenuChoice(choice, min, max);
                return choice;
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            } catch (IllegalArgumentException e) {
                System.out.print(e.getMessage() + " Try again: ");
            }
        }
    }

    // Reads a positive decimal number. Loops until a parseable value is entered.
    private static double readAmount() {
        while (true) {
            try {
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value <= 0) {
                    System.out.print("Amount must be greater than zero. Try again: $");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: $");
            }
        }
    }

    // Reads a positive integer for a given field label. Loops until valid.
    private static int readPositiveInt(String label) {
        while (true) {
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value <= 0) {
                    System.out.print("Please enter a positive number for " + label + ": ");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number for " + label + ": ");
            }
        }
    }

    // Pauses and waits for the user to press Enter before returning to the menu.
    private static void pressEnterToContinue() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    // Prints a formatted error message.
    private static void printError(String message) {
        System.out.println("\nERROR: " + message);
    }
}
