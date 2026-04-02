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
            printMainMenu();
            int choice = inputReader.readMenuChoice(1, 7);

            switch (choice) {
                case 1 -> handleManageAccounts();
                case 2 -> handleProcessTransaction();
                case 3 -> handleAccountStatements();
                case 4 -> handleSaveData();
                case 5 -> handleConcurrentSimulation();
                case 6 -> handleRunTests();
                case 7 -> running = false;
            }
        }

        persistenceService.saveAccounts(accountService.getAllAccounts());
        persistenceService.saveTransactions(accountService.getAllTransactions());
        System.out.println("\nThank you for using Bank Account Management System!");
        System.out.println("Data automatically saved to disk.");
        System.out.println("Goodbye!");
        inputReader.close();
    }

    // ── grouped sub-menu handlers ──────────────────────────────────────────────

    /** Runs the Manage Accounts sub-menu loop (create, view, close, fees & interest). */
    private void handleManageAccounts() {
        boolean inMenu = true;
        while (inMenu) {
            printManageAccountsMenu();
            int choice = inputReader.readMenuChoice(0, 5);
            switch (choice) {
                case 1 -> handleCreateAccount();
                case 2 -> handleViewAccounts();
                case 3 -> handleViewCustomerAccounts();
                case 4 -> handleCloseAccount();
                case 5 -> handleApplyFeesAndInterest();
                case 0 -> inMenu = false;
            }
        }
    }

    /** Runs the Account Statements sub-menu loop (generate statement, view transaction history). */
    private void handleAccountStatements() {
        boolean inMenu = true;
        while (inMenu) {
            printAccountStatementsMenu();
            int choice = inputReader.readMenuChoice(0, 2);
            switch (choice) {
                case 1 -> handleGenerateStatement();
                case 2 -> handleViewTransactionHistory();
                case 0 -> inMenu = false;
            }
        }
    }

    // ── menu handlers ──────────────────────────────────────────────────────────

    /** Launches the full JUnit test suite via Maven and streams output to the console. */
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

    /** Persists all accounts and transactions to disk immediately. */
    private void handleSaveData() {
        System.out.println("\nSAVING ACCOUNT DATA");
        System.out.println("____________________");
        persistenceService.saveAccounts(accountService.getAllAccounts());
        persistenceService.saveTransactions(accountService.getAllTransactions());
        System.out.println("\u2713 File save completed successfully.");
        inputReader.pressEnterToContinue();
    }

    /** Prompts for a simulation type and delegates to {@link com.bank_management_system.utils.ConcurrencyUtils}. */
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

    /** Displays a formatted table of all bank accounts. */
    private void handleViewAccounts() {
        System.out.println("\n--- ALL BANK ACCOUNTS ---");
        accountService.displayAllAccounts();
        inputReader.pressEnterToContinue();
    }

    /** Collects account number, transaction type, and amount, then delegates to the service layer. */
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

    /**
     * Collects the destination account and transfer amount, confirms with the user,
     * then delegates to {@link com.bank_management_system.accounts.AccountService#transfer}.
     *
     * @param fromAccNum  the source account number (already validated by the caller)
     * @param fromBalance the source account's balance before the transfer (for the confirmation display)
     */
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

    /** Reads an account number and prints its full account statement via {@link com.bank_management_system.services.StatementGenerator}. */
    private void handleGenerateStatement() {
        System.out.println("\n--- GENERATE ACCOUNT STATEMENT ---");
        System.out.print("Enter Account Number: ");
        String accNum = inputReader.nextLine();

        try {
            InputValidator.validateAccountNumber(accNum);
            accountService.generateAccountStatement(accNum);
        } catch (Exception e) {
            printError(e.getMessage());
        }
        inputReader.pressEnterToContinue();
    }

    /** Reads an account number and sort preference, then displays the transaction history table. */
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

    /** Prompts the user to choose a sort order and returns "DATE" or "AMOUNT". */
    private String readSortPreference() {
        System.out.println("Sort transactions by:");
        System.out.println("  1. Date (newest first)");
        System.out.println("  2. Amount (highest first)");
        System.out.print("Select (1-2): ");
        int choice = inputReader.readMenuChoice(1, 2);
        return choice == 2 ? "AMOUNT" : "DATE";
    }

    /** Handles account creation — either for an existing customer or by registering a new one. */
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

    /** Confirms balance is zero, prompts for confirmation, then closes the account. */
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

            if (Math.abs(account.getBalance()) > 0.001) {
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

    /** Confirms with the user, then applies monthly fees to all eligible Checking accounts and credits interest to all active Savings accounts. */
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

    /** Looks up a customer by ID and displays their account portfolio. */
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

    /** Prints a one-line account summary (customer name, type, current balance) before a transaction. */
    private void printAccountSummary(Account account) {
        System.out.println("\nAccount Details:");
        System.out.printf("Customer: %s | Account Type: %s | Current Balance: $%,.2f%n",
                account.getCustomerName(), account.getAccountType(), account.getBalance());
    }

    /** Prompts the user to choose a transaction type and returns "DEPOSIT", "WITHDRAWAL", or "TRANSFER". */
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

    /** Prompts for a positive transaction amount and returns the validated value. */
    private double readTransactionAmount() {
        System.out.print("Enter amount: $");
        return inputReader.readAmount();
    }

    /**
     * Shows a transaction confirmation screen and waits for Y/N.
     *
     * @param accNum          the account number
     * @param txnType         the transaction type (DEPOSIT or WITHDRAWAL)
     * @param amount          the transaction amount
     * @param previousBalance the balance before the transaction
     * @return true if the user confirmed; false if cancelled
     */
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

    /**
     * Prompts for a customer ID and looks the customer up in the system.
     *
     * @return the found Customer, or null if not found (error already printed)
     */
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

    /**
     * Collects all required fields for a new customer and registers them via {@link CustomerService}.
     *
     * @return the newly registered Customer
     */
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

    /**
     * Loops until the user enters a syntactically valid email address.
     *
     * @return the validated email string
     */
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

    /**
     * Prompts for account type and initial balance, then creates the account via the service layer.
     *
     * @param customer the customer to open the account for
     * @return the newly created Account
     */
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

    /** Prints a formatted confirmation box after a new account is successfully created. */
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

    private void printMainMenu() {
        System.out.println("\n==============================================");
        System.out.println("               MAIN MENU                      ");
        System.out.println("==============================================");
        System.out.println("  1. Manage Accounts");
        System.out.println("  2. Perform Transactions");
        System.out.println("  3. Account Statements");
        System.out.println("  4. Save Data");
        System.out.println("  5. Run Concurrent Simulation");
        System.out.println("  6. Run Tests");
        System.out.println("  7. Exit");
        System.out.println("==============================================");
        System.out.print("Enter choice: ");
    }

    private void printManageAccountsMenu() {
        System.out.println("\n--- MANAGE ACCOUNTS ---");
        System.out.println("  1. Create Account");
        System.out.println("  2. View All Accounts");
        System.out.println("  3. View Customer Accounts");
        System.out.println("  4. Close Account");
        System.out.println("  5. Apply Monthly Fees & Interest");
        System.out.println("  0. Back to Main Menu");
        System.out.print("Enter choice: ");
    }

    private void printAccountStatementsMenu() {
        System.out.println("\n--- ACCOUNT STATEMENTS ---");
        System.out.println("  1. Generate Account Statement");
        System.out.println("  2. View Transaction History");
        System.out.println("  0. Back to Main Menu");
        System.out.print("Enter choice: ");
    }

    private void printError(String message) {
        System.out.println("\nERROR: " + message);
    }
}
