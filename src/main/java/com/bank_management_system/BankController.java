package com.bank_management_system;

import com.bank_management_system.accounts.Account;
import com.bank_management_system.accounts.AccountService;
import com.bank_management_system.accounts.CheckingAccount;
import com.bank_management_system.accounts.SavingsAccount;
import com.bank_management_system.customers.Customer;
import com.bank_management_system.customers.CustomerService;
import com.bank_management_system.exceptions.InputValidator;
import com.bank_management_system.persistence.FilePersistenceService;
import com.bank_management_system.utils.ConcurrencyUtils;
import com.bank_management_system.utils.ValidationUtils;

import java.io.File;

/**
 * UI Controller for the Bank Management System.
 * Drives the main menu loop and delegates all business logic to the service layer.
 */
public class BankController {

    private final AccountService accountService;
    private final CustomerService customerService;
    private final InputReader inputReader;
    private final FilePersistenceService persistenceService;

    public BankController(AccountService accountService, CustomerService customerService,
                          InputReader inputReader, FilePersistenceService persistenceService) {
        this.accountService     = accountService;
        this.customerService    = customerService;
        this.inputReader        = inputReader;
        this.persistenceService = persistenceService;
    }

    /**
     * Starts the main menu loop, routing each user selection to the appropriate handler.
     * Runs until the user chooses to exit.
     */
    public void start() {
        printWelcome();

        boolean running = true;
        while (running) {
            printMenu();
            int choice = inputReader.readMenuChoice(1, 11);

            switch (choice) {
                case 1  -> handleCreateAccount();
                case 2  -> handleViewAccounts();
                case 3  -> handleProcessTransaction();
                case 4  -> handleViewTransactionHistory();
                case 5  -> handleCloseAccount();
                case 6  -> handleApplyFeesAndInterest();
                case 7  -> handleViewCustomerAccounts();
                case 8  -> handleRunTests();
                case 9  -> handleSaveData();
                case 10 -> handleConcurrentSimulation();
                case 11 -> running = false;
            }
        }

        persistenceService.saveAccounts(accountService.getAllAccounts());
        persistenceService.saveTransactions(accountService.getAllTransactions());
        System.out.println("\nThank you for using Bank Account Management System!");
        System.out.println("Data automatically saved to disk.");
        System.out.println("Goodbye!");
        inputReader.close();
    }

    // ── menu handlers ──────────────────────────────────────────────────────────

    private void handleRunTests() {
        System.out.println("\n--- RUN TEST SUITE ---");
        System.out.println("Launching JUnit tests via Maven...\n");
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "mvn", "test");
            pb.directory(new File(System.getProperty("user.dir")));
            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();
        } catch (Exception e) {
            printError("Could not run tests: " + e.getMessage());
        }
        inputReader.pressEnterToContinue();
    }

    private void handleSaveData() {
        System.out.println("\nSAVING ACCOUNT DATA");
        System.out.println("____________________");
        persistenceService.saveAccounts(accountService.getAllAccounts());
        persistenceService.saveTransactions(accountService.getAllTransactions());
        System.out.println("\u2713 File save completed successfully.");
        inputReader.pressEnterToContinue();
    }

    private void handleConcurrentSimulation() {
        System.out.println("\n--- RUN CONCURRENT SIMULATION ---");
        System.out.println("  1. Single account thread simulation");
        System.out.println("  2. Parallel stream batch deposit (all accounts)");
        System.out.print("Select (1-2): ");
        int choice = inputReader.readMenuChoice(1, 2);

        try {
            if (choice == 1) {
                System.out.print("\nEnter Account Number: ");
                String accNum = inputReader.nextLine();
                InputValidator.validateAccountNumber(accNum);
                Account account = accountService.getAccountDetails(accNum);
                System.out.printf("%nAccount : %s (%s — %s)%n",
                        accNum, account.getCustomerName(), account.getAccountType());
                System.out.printf("Balance : $%,.2f%n%n", account.getBalance());
                ConcurrencyUtils.runSimulation(account);
            } else {
                ConcurrencyUtils.runParallelBatchSimulation(accountService.getAllAccounts());
            }
        } catch (Exception e) {
            printError(e.getMessage());
        }
        inputReader.pressEnterToContinue();
    }

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
            InputValidator.validateAccountNumber(accNum);
            Account account = accountService.getAccountDetails(accNum);
            printAccountSummary(account);

            String txnType = readTransactionType();

            if ("TRANSFER".equals(txnType)) {
                handleTransfer(accNum, account.getBalance());
                inputReader.pressEnterToContinue();
                return;
            }

            double amount = readTransactionAmount();

            if (!confirmTransaction(accNum, txnType, amount, account.getBalance())) {
                return;
            }

            accountService.processTransaction(accNum, amount, txnType);
            System.out.println("\nTransaction completed successfully!");

        } catch (Exception e) {
            printError(e.getMessage());
        }
        inputReader.pressEnterToContinue();
    }

    private void handleTransfer(String fromAccNum, double fromBalance) {
        System.out.print("Enter destination Account Number: ");
        String toAccNum = inputReader.nextLine();

        try {
            InputValidator.validateAccountNumber(toAccNum);
            Account destination = accountService.getAccountDetails(toAccNum);

            System.out.printf("Destination: %s (%s) | Balance: $%,.2f%n",
                    destination.getCustomerName(), destination.getAccountType(), destination.getBalance());

            System.out.print("Enter transfer amount: $");
            double amount = inputReader.readAmount();

            System.out.println("\nTRANSFER CONFIRMATION");
            System.out.println("-".repeat(50));
            System.out.printf("From  : %s | $%,.2f → $%,.2f%n", fromAccNum, fromBalance, fromBalance - amount);
            System.out.printf("To    : %s | $%,.2f → $%,.2f%n", toAccNum,   destination.getBalance(), destination.getBalance() + amount);
            System.out.printf("Amount: $%,.2f%n", amount);
            System.out.print("\nConfirm transfer? (Y/N): ");

            if (!inputReader.nextLine().equalsIgnoreCase("Y")) {
                System.out.println("Transfer cancelled.");
                return;
            }

            accountService.transfer(fromAccNum, toAccNum, amount);
            System.out.println("\nTransfer completed successfully!");

        } catch (Exception e) {
            printError(e.getMessage());
        }
    }

    private void handleViewTransactionHistory() {
        System.out.println("\n--- VIEW TRANSACTION HISTORY ---");
        System.out.print("Enter Account Number: ");
        String accNum = inputReader.nextLine();

        try {
            InputValidator.validateAccountNumber(accNum);
            Account account = accountService.getAccountDetails(accNum);

            System.out.println("\nAccount: " + accNum + " - " + account.getCustomerName());
            System.out.println("Account Type: " + account.getAccountType());
            System.out.printf("Current Balance: $%,.2f%n", account.getBalance());
            System.out.println();

            String sortBy = readSortPreference();
            accountService.getTransactionHistory(accNum, sortBy);

        } catch (Exception e) {
            printError(e.getMessage());
        }
        inputReader.pressEnterToContinue();
    }

    private String readSortPreference() {
        System.out.println("Sort transactions by:");
        System.out.println("  1. Date (newest first)");
        System.out.println("  2. Amount (highest first)");
        System.out.print("Select (1-2): ");
        int choice = inputReader.readMenuChoice(1, 2);
        return choice == 2 ? "AMOUNT" : "DATE";
    }

    private void handleCreateAccount() {
        System.out.println("\n--- ACCOUNT CREATION ---");
        System.out.print("Is this an existing customer? (Y/N): ");
        String existingChoice = inputReader.nextLine();

        try {
            Customer customer = existingChoice.equalsIgnoreCase("Y")
                    ? lookUpExistingCustomer()
                    : registerNewCustomer();

            if (customer == null) return;

            Account account = openAccount(customer);
            printAccountCreatedConfirmation(customer, account);

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
            InputValidator.validateAccountNumber(accNum);
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
            if (!inputReader.nextLine().equalsIgnoreCase("Y")) {
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

        if (!inputReader.nextLine().equalsIgnoreCase("Y")) {
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
            InputValidator.validateCustomerId(customerId);
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

    // ── handleProcessTransaction helpers ───────────────────────────────────────

    private void printAccountSummary(Account account) {
        System.out.println("\nAccount Details:");
        System.out.printf("Customer: %s | Account Type: %s | Current Balance: $%,.2f%n",
                account.getCustomerName(), account.getAccountType(), account.getBalance());
    }

    private String readTransactionType() {
        System.out.println("\nTransaction type:");
        System.out.println("  1. Deposit");
        System.out.println("  2. Withdrawal");
        System.out.println("  3. Transfer to another account");
        System.out.print("Select type (1-3): ");
        int type = inputReader.readMenuChoice(1, 3);
        return switch (type) {
            case 1  -> "DEPOSIT";
            case 2  -> "WITHDRAWAL";
            default -> "TRANSFER";
        };
    }

    private double readTransactionAmount() {
        System.out.print("Enter amount: $");
        return inputReader.readAmount();
    }

    private boolean confirmTransaction(String accNum, String txnType, double amount, double previousBalance) {
        double expectedBalance = "DEPOSIT".equals(txnType) ? previousBalance + amount : previousBalance - amount;

        System.out.println("\nTRANSACTION CONFIRMATION");
        System.out.println("-".repeat(50));
        System.out.printf("Account: %s | Type: %s%n", accNum, txnType);
        System.out.printf("Amount: $%,.2f%n", amount);
        System.out.printf("Previous Balance: $%,.2f | New Balance: $%,.2f%n", previousBalance, expectedBalance);
        System.out.print("\nConfirm transaction? (Y/N): ");

        if (!inputReader.nextLine().equalsIgnoreCase("Y")) {
            System.out.println("Transaction cancelled.");
            inputReader.pressEnterToContinue();
            return false;
        }
        return true;
    }

    // ── handleCreateAccount helpers ────────────────────────────────────────────

    private Customer lookUpExistingCustomer() {
        System.out.print("Enter Customer ID (e.g. CUST001): ");
        String customerId = inputReader.nextLine();
        InputValidator.validateCustomerId(customerId);

        Customer customer = customerService.getAllCustomers().stream()
                .filter(c -> c.getCustomerId().equalsIgnoreCase(customerId))
                .findFirst()
                .orElse(null);

        if (customer == null) {
            printError("Customer not found: " + customerId);
            inputReader.pressEnterToContinue();
            return null;
        }

        System.out.printf("%nFound: %s (%s)%n", customer.getName(), customer.getCustomerType());
        return customer;
    }

    private Customer registerNewCustomer() {
        System.out.print("Enter customer name    : ");
        String name = inputReader.nextLine();

        System.out.print("Enter customer age     : ");
        int age = inputReader.readPositiveInt("age");

        System.out.print("Enter customer contact : ");
        String contact = inputReader.nextLine();

        String email = readValidatedEmail();

        System.out.print("Enter customer address : ");
        String address = inputReader.nextLine();

        System.out.println("\nCustomer type:");
        System.out.println("  1. Regular Customer (Standard banking services)");
        System.out.println("  2. Premium Customer (Enhanced benefits, min balance $10,000)");
        System.out.print("Select type (1-2): ");
        int customerType = inputReader.readMenuChoice(1, 2);

        return (customerType == 1)
                ? customerService.registerRegularCustomer(name, age, contact, email, address)
                : customerService.registerPremiumCustomer(name, age, contact, email, address);
    }

    private String readValidatedEmail() {
        while (true) {
            System.out.print("Enter customer email   : ");
            String email = inputReader.nextLine();
            if (ValidationUtils.isValidEmail.test(email)) {
                System.out.println("\u2713 Email accepted!");
                return email;
            }
            System.out.println("\u2717 Invalid email format. Please enter a valid address (e.g., name@example.com)");
        }
    }

    private Account openAccount(Customer customer) {
        System.out.println("\nAccount type:");
        System.out.printf("  1. Savings Account  (Interest: %.1f%%, Min Balance: $%.2f)%n", 3.5, 500.00);
        System.out.printf("  2. Checking Account (Overdraft: $%,.2f, Monthly Fee: $%.2f)%n", 1_000.00, 10.00);
        System.out.print("Select type (1-2): ");
        int accountType = inputReader.readMenuChoice(1, 2);

        System.out.print("Enter initial deposit amount: $");
        double initialBalance = inputReader.readAmount();

        return (accountType == 1)
                ? accountService.createSavingsAccount(customer.getCustomerId(), initialBalance)
                : accountService.createCheckingAccount(customer.getCustomerId(), initialBalance);
    }

    private void printAccountCreatedConfirmation(Customer customer, Account account) {
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
    }

    // ── ui helpers ─────────────────────────────────────────────────────────────

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
        System.out.println("  8. Run Tests");
        System.out.println("  9. Save Data");
        System.out.println(" 10. Run Concurrent Simulation");
        System.out.println(" 11. Exit");
        System.out.println("----------------------------------------------");
        System.out.print("Enter choice: ");
    }

    private void printError(String message) {
        System.out.println("\nERROR: " + message);
    }
}
