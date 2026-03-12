package com.bank_management_system;

import com.bank_management_system.accounts.Account;
import com.bank_management_system.accounts.AccountService;
import com.bank_management_system.accounts.CheckingAccount;
import com.bank_management_system.accounts.SavingsAccount;
import com.bank_management_system.customers.Customer;
import com.bank_management_system.customers.CustomerService;

// UI Controller for the Bank Management System.
// Drives the main menu loop and delegates to service layer methods.
public class BankController {

    private final AccountService accountService;
    private final CustomerService customerService;
    private final InputReader inputReader;

    public BankController(AccountService accountService, CustomerService customerService, InputReader inputReader) {
        this.accountService  = accountService;
        this.customerService = customerService;
        this.inputReader     = inputReader;
    }

    public void start() {
        printWelcome();

        boolean running = true;
        while (running) {
            printMenu();
            int choice = inputReader.readMenuChoice(1, 8);

            switch (choice) {
                case 1 -> handleCreateAccount();
                case 2 -> handleViewAccounts();
                case 3 -> handleProcessTransaction();
                case 4 -> handleViewTransactionHistory();
                case 5 -> handleCloseAccount();
                case 6 -> handleApplyFeesAndInterest();
                case 7 -> handleViewCustomerAccounts();
                case 8 -> running = false;
            }
        }

        System.out.println("\nThank you for using Bank Account Management System!");
        System.out.println("Goodbye!");
        inputReader.close();
    }

    // menu handlers
    // menu handlers

    private void handleViewAccounts() {
        System.out.println("\n--- ALL BANK ACCOUNTS ---");
        accountService.displayAllAccounts();
        inputReader.pressEnterToContinue();
    }

    private void handleProcessTransaction() {
        System.out.println("\n--- PROCESS TRANSACTION ---");
        System.out.print("Enter Account Number: ");
        String accNum = inputReader.nextLine();

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
            int type = inputReader.readMenuChoice(1, 2);

            System.out.print("Enter amount: $");
            double amount = inputReader.readAmount();

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
            String confirm = inputReader.nextLine();

            if (!confirm.equalsIgnoreCase("Y")) {
                System.out.println("Transaction cancelled.");
                inputReader.pressEnterToContinue();
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
        inputReader.pressEnterToContinue();
    }

    private void handleViewTransactionHistory() {
        System.out.println("\n--- VIEW TRANSACTION HISTORY ---");
        System.out.print("Enter Account Number: ");
        String accNum = inputReader.nextLine();

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
        inputReader.pressEnterToContinue();
    }

    private void handleCreateAccount() {
        System.out.println("\n--- ACCOUNT CREATION ---");

        // Collect customer details
        System.out.print("Enter customer name    : ");
        String name = inputReader.nextLine();

        System.out.print("Enter customer age     : ");
        int age = inputReader.readPositiveInt("age");

        System.out.print("Enter customer contact : ");
        String contact = inputReader.nextLine();

        System.out.print("Enter customer address : ");
        String address = inputReader.nextLine();

        // Customer type
        System.out.println("\nCustomer type:");
        System.out.println("  1. Regular Customer (Standard banking services)");
        System.out.println("  2. Premium Customer (Enhanced benefits, min balance $10,000)");
        System.out.print("Select type (1-2): ");
        int customerType = inputReader.readMenuChoice(1, 2);

        // Account type
        System.out.println("\nAccount type:");
        System.out.printf("  1. Savings Account  (Interest: %.1f%%, Min Balance: $%.2f)%n", 3.5, 500.00);
        System.out.printf("  2. Checking Account (Overdraft: $%,.2f, Monthly Fee: $%.2f)%n", 1_000.00, 10.00);
        System.out.print("Select type (1-2): ");
        int accountType = inputReader.readMenuChoice(1, 2);

        System.out.print("Enter initial deposit amount: $");
        double initialBalance = inputReader.readAmount();

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
        inputReader.pressEnterToContinue();
    }

    private void handleCloseAccount() {
        System.out.println("\n--- CLOSE ACCOUNT ---");
        System.out.print("Enter Account Number: ");
        String accNum = inputReader.nextLine();

        try {
            Account account = accountService.getAccountDetails(accNum);

            System.out.println("\nAccount Details:");
            System.out.printf("Customer: %s | Account Type: %s | Balance: $%,.2f | Status: %s%n",
                    account.getCustomerName(), account.getAccountType(),
                    account.getBalance(), account.getStatus());

            if (account.getBalance() != 0) {
                printError(String.format(
                        "Cannot close account. Balance must be $0.00 before closing. Current balance: $%,.2f",
                        account.getBalance()));
                inputReader.pressEnterToContinue();
                return;
            }

            System.out.print("\nPermanently close account " + accNum + "? This cannot be undone. (Y/N): ");
            String confirm = inputReader.nextLine();

            if (!confirm.equalsIgnoreCase("Y")) {
                System.out.println("Account closure cancelled.");
                inputReader.pressEnterToContinue();
                return;
            }

            accountService.closeAccount(accNum);

        } catch (Exception e) {
            printError(e.getMessage());
        }
        inputReader.pressEnterToContinue();
    }

    private void handleApplyFeesAndInterest() {
        System.out.println("\n--- APPLY MONTHLY FEES & INTEREST ---");
        System.out.println("This operation will:");
        System.out.println("  - Deduct $10.00 monthly fee from all Checking accounts (where not waived)");
        System.out.println("  - Credit 3.5% interest to all active Savings accounts");
        System.out.print("\nProceed? (Y/N): ");
        String confirm = inputReader.nextLine();

        if (!confirm.equalsIgnoreCase("Y")) {
            System.out.println("Operation cancelled.");
            inputReader.pressEnterToContinue();
            return;
        }

        try {
            int feesApplied     = accountService.applyMonthlyFees();
            int interestApplied = accountService.applyInterest();
            System.out.printf("%nMonthly fees applied : %d account(s)%n", feesApplied);
            System.out.printf("Interest credited    : %d account(s)%n", interestApplied);
            System.out.println("Done. Transactions recorded in ledger.");
        } catch (Exception e) {
            printError(e.getMessage());
        }
        inputReader.pressEnterToContinue();
    }

    private void handleViewCustomerAccounts() {
        System.out.println("\n--- VIEW CUSTOMER ACCOUNTS ---");
        System.out.print("Enter Customer ID (e.g. CUST001): ");
        String customerId = inputReader.nextLine();

        try {
            customerService.getAllCustomers().stream()
                    .filter(c -> c.getCustomerId().equalsIgnoreCase(customerId))
                    .findFirst()
                    .ifPresentOrElse(
                            customer -> {
                                System.out.printf("%nCustomer: %s (%s)%n",
                                        customer.getName(), customer.getCustomerType());
                                customer.viewCustomerAccounts();
                            },
                            () -> printError("Customer not found: " + customerId)
                    );
        } catch (Exception e) {
            printError(e.getMessage());
        }
        inputReader.pressEnterToContinue();
    }

    // ui methods
    // ui methods

    private void printWelcome() {
        System.out.println("==============================================");
        System.out.println("|   BANK ACCOUNT MANAGEMENT SYSTEM           |");
        System.out.println("==============================================");
    }

    private void printMenu() {
        System.out.println("                MAIN MENU                     ");
        System.out.println("----------------------------------------------");
        System.out.println("  1. Create Account");
        System.out.println("  2. View Accounts");
        System.out.println("  3. Process Transaction");
        System.out.println("  4. View Transaction History");
        System.out.println("  5. Close Account");
        System.out.println("  6. Apply Monthly Fees & Interest");
        System.out.println("  7. View Customer Accounts");
        System.out.println("  8. Exit");
        System.out.println("----------------------------------------------");
        System.out.print("Enter choice: ");
    }

    // Prints a formatted error message.
    private void printError(String message) {
        System.out.println("\nERROR: " + message);
    }
}
