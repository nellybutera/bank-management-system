package com.bank_management_system;

import com.bank_management_system.bank.Bank;

import java.util.InputMismatchException;
import java.util.Scanner;

import com.bank_management_system.accounts.Account;
import com.bank_management_system.accounts.AccountManager;
import com.bank_management_system.accounts.AccountService;
import com.bank_management_system.accounts.CheckingAccount;
import com.bank_management_system.customers.Customer;
import com.bank_management_system.customers.CustomerService;
import com.bank_management_system.customers.RegularCustomer;
import com.bank_management_system.shared.InputValidator;
import com.bank_management_system.shared.InvalidAmountException;
import com.bank_management_system.transactions.TransactionManager;

public class Main {
    private static final Bank bank = new Bank();
    private static final AccountManager accountManager = new AccountManager();
    private static final TransactionManager transactionManager = new TransactionManager();
    private static final AccountService accountService = new AccountService(bank, accountManager, transactionManager);

    private static final CustomerService customerService = new CustomerService(bank);

    private static final Scanner scanner = new Scanner(System.in);

    private static void handleViewAccounts() {
        System.out.println("\n--- ALL BANK ACCOUNTS ---");
        accountService.displayAllAccounts();
        pressEnterToContinue();
    }

    private static void handleProcessTransaction() {
        System.out.print("Enter account number: ");
        String accNum = scanner.nextLine().trim();

        try {
            // Using your updated method name 'getAccountDetails'
            Account account = accountService.getAccountDetails(accNum);

            System.out.printf("Current Balance: $%,.2f%n", account.getBalance());
            System.out.print("1. Deposit\n2. Withdraw\nChoice: ");
            int type = readMenuChoice(1, 2);
            
            System.out.print("Amount: $");
            double amount = readAmount();

            if (type == 1) {
                accountService.deposit(accNum, amount);
            } else {
                accountService.withdraw(accNum, amount);
            }
            
            System.out.println("Transaction Successful. New Balance: $" + account.getBalance());

        } catch (Exception e) {
            printError(e.getMessage());
        }
    }
        
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
        scanner.close();
    }

    private static void initializeSampleData() {
        try {
            RegularCustomer c1 = customerService.registerRegularCustomer(
                    "John Smith",     35, "555-1234", "123 Main St, Springfield");
            RegularCustomer c2 = customerService.registerRegularCustomer(
                    "Sarah Johnson",  28, "555-2345", "456 Oak Ave, Riverside");
            RegularCustomer c3 = customerService.registerRegularCustomer(
                    "Michael Chen",   42, "555-3456", "789 Pine Rd, Lakewood");
            RegularCustomer c4 = customerService.registerRegularCustomer(
                    "Emily Brown",    31, "555-4567", "321 Elm St, Hillside");
            RegularCustomer c5 = customerService.registerRegularCustomer(
                    "David Wilson",   55, "555-5678", "654 Maple Dr, Westfield");

            bank.addCustomer(c1);
            bank.addCustomer(c2);
            bank.addCustomer(c3);
            bank.addCustomer(c4);
            bank.addCustomer(c5);

            accountService.createSavingsAccount(c1.getCustomerId(),  5_250.00);
            accountService.createCheckingAccount(c2.getCustomerId(), 3_450.00);
            accountService.createSavingsAccount(c3.getCustomerId(), 15_750.00);
            accountService.createCheckingAccount(c4.getCustomerId(),   890.00);
            accountService.createSavingsAccount(c5.getCustomerId(), 25_300.00);

        } catch (Exception e) {
            System.err.println("Error loading sample data: " + e.getMessage());
        }
    }

    private static void handleViewTransactionHistory() {
        System.out.print("Enter account number: ");
        String accNum = scanner.nextLine().trim();
        
        try {
            // Your new Service method handles the printing now!
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

        // Customer type selection
        System.out.println("\nCustomer type:");
        System.out.println("  1. Regular Customer (Standard banking services)");
        System.out.println("  2. Premium Customer (Enhanced benefits, min balance $10,000)");
        System.out.print("Select type (1-2): ");
        int customerType = readMenuChoice(1, 2);

                // Account type selection
        System.out.println("\nAccount type:");
        System.out.printf("  1. Savings Account  (Interest: %.1f%%, Min Balance: $%.2f)%n",
                3.5, 500.00);
        System.out.printf("  2. Checking Account (Overdraft: $%,.2f, Monthly Fee: $%.2f)%n",
                1_000.00, 10.00);
        System.out.print("Select type (1-2): ");
        int accountType = readMenuChoice(1, 2);

        System.out.print("Enter initial deposit amount: $");
        double initialBalance = readAmount();

        try {
            InputValidator.validateName(name);
            InputValidator.validateId(contact, "Contact");
            InputValidator.validateId(address, "Address");

            // Register the customer
            Customer customer;
            if (customerType == 1) {
                customer = customerService.registerRegularCustomer(name, age, contact, address);
            } else {
                customer = customerService.registerPremiumCustomer(name, age, contact, address);
            }

            // Create the account
            Account account;
            if (accountType == 1) {
                account = accountService.createSavingsAccount(customer.getCustomerId(), initialBalance);
            } else {
                account = accountService.createCheckingAccount(customer.getCustomerId(), initialBalance);
            }

            // Confirmation
            System.out.println("\n Account created successfully!");
            System.out.println("=".repeat(50));
            System.out.println(" Account Number : " + account.getAccountNumber());
            System.out.println(" Customer       : " + customer.getName());
            System.out.println(" Account Type   : " + account.getAccountType());
            System.out.printf( " Balance        : $%,.2f%n", account.getBalance());
            System.out.println(" Status         : " + account.getStatus());

            if (account instanceof CheckingAccount ca) {
                System.out.printf(" Monthly Fee    : %s%n", 
                    ca.isFeesWaived() ? "WAIVED" : "$10.00");
            }
            System.out.println("=".repeat(50));
            } catch (Exception e) {
            printError(e.getMessage());
        }
        pressEnterToContinue();
    }


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

    private static int readMenuChoice(int min, int max) {
        while (true) {
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                InputValidator.validateMenuChoice(choice, min, max);
                return choice;
            } catch (InputMismatchException | NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            } catch (InvalidAmountException e) {
                System.out.print(e.getMessage() + " Try again: ");
            }
        }
    }
        /** Reads a positive decimal number. Loops until a parseable value is entered. */
    private static double readAmount() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

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

    // Pauses and waits for the user to press Enter before returning to the menu. */
    private static void pressEnterToContinue() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

        /** Prints a formatted error message. */
    private static void printError(String message) {
        System.out.println("ERROR: " + message);
    }
}