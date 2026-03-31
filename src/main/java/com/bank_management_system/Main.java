package com.bank_management_system;

import com.bank_management_system.accounts.Account;
import com.bank_management_system.accounts.AccountManager;
import com.bank_management_system.accounts.AccountService;
import com.bank_management_system.bank.Bank;
import com.bank_management_system.customers.Customer;
import com.bank_management_system.customers.CustomerService;
import com.bank_management_system.persistence.FilePersistenceService;
import com.bank_management_system.transactions.Transaction;
import com.bank_management_system.transactions.TransactionManager;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        Bank               bank               = new Bank();
        AccountManager     accountManager     = new AccountManager();
        TransactionManager transactionManager = new TransactionManager();
        AccountService     accountService     = new AccountService(bank, accountManager, transactionManager);
        CustomerService    customerService    = new CustomerService(bank);
        InputReader        inputReader        = new InputReader();
        FilePersistenceService persistenceService = new FilePersistenceService(bank);

        List<Account>     loadedAccounts     = persistenceService.loadAccounts();
        List<Transaction> loadedTransactions = persistenceService.loadTransactions();

        if (loadedAccounts.isEmpty()) {
            System.out.println("No saved data found — loading sample data.");
            try {
                DataInitializer.initializeSampleData(accountService, customerService);
            } catch (Exception e) {
                System.err.println("Failed to load sample data: " + e.getMessage());
                return;
            }
        } else {
            System.out.println("Data loaded: " + loadedAccounts.size() + " account(s), "
                    + loadedTransactions.size() + " transaction(s).");
            loadedAccounts.forEach(accountManager::addAccount);
            loadedTransactions.forEach(transactionManager::addTransaction);
            resetCounters(loadedAccounts, loadedTransactions);
        }

        BankController controller = new BankController(accountService, customerService, inputReader, persistenceService);
        controller.start();
    }

    private static void resetCounters(List<Account> accounts, List<Transaction> transactions) {
        int maxAcc = accounts.stream()
                .mapToInt(a -> Integer.parseInt(a.getAccountNumber().substring(3)))
                .max().orElse(0);
        Account.resetCounter(maxAcc + 1);

        int maxCust = accounts.stream()
                .mapToInt(a -> Integer.parseInt(a.getCustomerId().substring(4)))
                .max().orElse(0);
        Customer.resetCounter(maxCust + 1);

        int maxTxn = transactions.stream()
                .mapToInt(t -> Integer.parseInt(t.getTransactionId().substring(3)))
                .max().orElse(0);
        Transaction.resetCounter(maxTxn + 1);
    }
}
