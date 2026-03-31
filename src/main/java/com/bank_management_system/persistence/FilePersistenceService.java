package com.bank_management_system.persistence;

import com.bank_management_system.accounts.Account;
import com.bank_management_system.accounts.CheckingAccount;
import com.bank_management_system.accounts.SavingsAccount;
import com.bank_management_system.bank.Bank;
import com.bank_management_system.customers.Customer;
import com.bank_management_system.customers.PremiumCustomer;
import com.bank_management_system.customers.RegularCustomer;
import com.bank_management_system.transactions.Transaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FilePersistenceService {

    private static final String ACCOUNTS_FILE     = "accounts.txt";
    private static final String TRANSACTIONS_FILE = "transactions.txt";

    private final Bank bank;
    private final Map<String, Customer> customerCache = new HashMap<>();

    public FilePersistenceService(Bank bank) {
        this.bank = bank;
    }

    // ── save ──────────────────────────────────────────────────────────────────

    public void saveAccounts(Collection<Account> accounts) {
        List<String> lines = accounts.stream()
                .map(Account::toFileLine)
                .collect(Collectors.toList());
        try {
            Files.write(Paths.get(ACCOUNTS_FILE), lines);
            System.out.println("Accounts saved  -> " + ACCOUNTS_FILE + " (" + lines.size() + " records)");
        } catch (IOException e) {
            System.err.println("Failed to save accounts: " + e.getMessage());
        }
    }

    public void saveTransactions(List<Transaction> transactions) {
        List<String> lines = transactions.stream()
                .map(Transaction::toFileLine)
                .collect(Collectors.toList());
        try {
            Files.write(Paths.get(TRANSACTIONS_FILE), lines);
            System.out.println("Transactions saved -> " + TRANSACTIONS_FILE + " (" + lines.size() + " records)");
        } catch (IOException e) {
            System.err.println("Failed to save transactions: " + e.getMessage());
        }
    }

    // ── load ──────────────────────────────────────────────────────────────────

    /**
     * Loads all accounts from accounts.txt.
     * Each line is mapped to an Account object via {@code this::parseAccount}.
     * Customers are reconstructed and registered with the Bank automatically.
     *
     * @return the list of restored accounts, or an empty list if the file does not exist
     */
    public List<Account> loadAccounts() {
        if (!Files.exists(Paths.get(ACCOUNTS_FILE))) return Collections.emptyList();
        try {
            return Files.lines(Paths.get(ACCOUNTS_FILE))
                    .filter(line -> !line.isBlank())
                    .map(this::parseAccount)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Failed to load accounts: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Loads all transactions from transactions.txt.
     * Each line is mapped to a Transaction using the {@code Transaction::fromLine} method reference.
     *
     * @return the list of restored transactions, or an empty list if the file does not exist
     */
    public List<Transaction> loadTransactions() {
        if (!Files.exists(Paths.get(TRANSACTIONS_FILE))) return Collections.emptyList();
        try {
            return Files.lines(Paths.get(TRANSACTIONS_FILE))
                    .filter(line -> !line.isBlank())
                    .map(Transaction::fromLine)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Failed to load transactions: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // ── parsing ───────────────────────────────────────────────────────────────

    /**
     * Parses a single pipe-delimited account line and reconstructs the Account object.
     * Format (Savings):  SAVINGS|accNum|custId|custName|custType|age|contact|address|balance|status
     * Format (Checking): CHECKING|accNum|custId|custName|custType|age|contact|address|balance|status|feesWaived
     */
    private Account parseAccount(String line) {
        String[] p       = line.split("\\|");
        String type      = p[0];
        String accNum    = p[1];
        String custId    = p[2];
        String custName  = p[3];
        String custType  = p[4];
        int    age       = Integer.parseInt(p[5]);
        String contact   = p[6];
        String address   = p[7];
        double balance   = Double.parseDouble(p[8]);
        String status    = p[9];

        Customer customer = customerCache.computeIfAbsent(custId, id -> {
            Customer c = custType.equals("Premium")
                    ? new PremiumCustomer(id, custName, age, contact, address)
                    : new RegularCustomer(id, custName, age, contact, address);
            bank.addCustomer(c);
            return c;
        });

        Account account;
        if (type.equals("SAVINGS")) {
            account = new SavingsAccount(accNum, customer, balance, status);
        } else {
            boolean feesWaived = Boolean.parseBoolean(p[10]);
            account = new CheckingAccount(accNum, customer, balance, status, feesWaived);
        }

        customer.addAccount(account);
        return account;
    }
}
