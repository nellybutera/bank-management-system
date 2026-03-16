package com.bank_management_system.accounts;

import com.bank_management_system.bank.Bank;
import com.bank_management_system.customers.Customer;
import com.bank_management_system.shared.InvalidAmountException;
import com.bank_management_system.shared.IllegalStateException;
import com.bank_management_system.transactions.Transaction;
import com.bank_management_system.transactions.TransactionManager;

public class AccountService {
    private final Bank bank;
    private final AccountManager accountManager;
    private final TransactionManager transactionManager;

    public AccountService(Bank bank, AccountManager accountManager, TransactionManager transactionManager){
        this.bank = bank;
        this.accountManager = accountManager;
        this.transactionManager = transactionManager;
    }

    // creating a new savings account for a customer
    public SavingsAccount createSavingsAccount(String customerId, double initialBalance){
        Customer customer = bank.findCustomerById(customerId);

        if(initialBalance < SavingsAccount.getMinimumBalance()){
            throw new InvalidAmountException(String.format(
                    "Initial balance for a Savings account must be at least $%.2f. Provided: $%.2f",
                    SavingsAccount.getMinimumBalance(), initialBalance));
        }

        SavingsAccount account = new SavingsAccount(customer, initialBalance);
        accountManager.addAccount(account);
        customer.addAccount(account);
        return account;
    }

    // this methods creates a new checking account
    public CheckingAccount createCheckingAccount(String customerId, double initialBalance){
        Customer customer = bank.findCustomerById(customerId);

        if(initialBalance < 0){
            throw new InvalidAmountException("Initial balance for a checking account can not be negative. Please enter a valid amount. Provided: "+ initialBalance);
        }

        boolean feesWaived = customer.isEligibleForFeeWaiver();

        CheckingAccount account = new CheckingAccount(customer, initialBalance, feesWaived);
        accountManager.addAccount(account);
        customer.addAccount(account);
        return account;
    }

    // deposit method (financial operation)
    public void deposit(String accountNumber, double amount){
        Account account = accountManager.findAccountOrThrow(accountNumber);
        Transaction transaction = account.deposit(amount);
        transactionManager.addTransaction(transaction);
    }

    // withdrawal method
    public void withdraw(String accountNumber, double amount){
        Account account = accountManager.findAccountOrThrow(accountNumber);
        Transaction transaction = account.withdraw(amount);
        transactionManager.addTransaction(transaction);
    }

    // unified transaction entry point — routes to deposit or withdrawal and logs the result
    public void processTransaction(String accountNumber, double amount, String type){
        Account account = accountManager.findAccountOrThrow(accountNumber);
        Transaction transaction;
        if ("DEPOSIT".equalsIgnoreCase(type)) {
            transaction = account.deposit(amount);
        } else if ("WITHDRAWAL".equalsIgnoreCase(type)) {
            transaction = account.withdraw(amount);
        } else {
            throw new com.bank_management_system.shared.IllegalArgumentException(
                    "Invalid transaction type: " + type + ". Must be DEPOSIT or WITHDRAWAL.");
        }
        transactionManager.addTransaction(transaction);
    }

    // this returns the account details for a given account number
    public Account getAccountDetails(String accountNumber){
        return accountManager.findAccountOrThrow(accountNumber);
    }

    // returns transaction history for a given account number
    public void getTransactionHistory(String accountNumber){
        accountManager.findAccountOrThrow(accountNumber);
        transactionManager.viewTransactionsByAccount(accountNumber);
    }

    // // exposes the accountmanager class so that Main can call viewAllAccounts() directly;
    // public AccountManager getAccountManager(){
    //     return accountManager;
    // }

    // method to view accounts in main
    public void displayAllAccounts(){
        accountManager.viewAllAccounts();
    }

    // additional method to close an account (soft delete — sets status to "Closed")
    public void closeAccount(String accountNumber){
        Account account = accountManager.findAccountOrThrow(accountNumber);

        if(account.getBalance() != 0){
            throw new IllegalStateException("Withdraw all funds before closing.");
        }

        account.closeAccount();
        System.out.println("Account " + accountNumber + " is now closed");
    }

    // deducts the $10 monthly fee from every non-waived CheckingAccount and logs each as a transaction
    public int applyMonthlyFees(){
        Account[] accounts = accountManager.getAccounts();
        int count = 0;
        for (Account account : accounts) {
            if (account instanceof CheckingAccount ca && account.getStatus().equalsIgnoreCase("Active")) {
                Transaction transaction = ca.applyMonthlyFee();
                if (transaction != null) {
                    transactionManager.addTransaction(transaction);
                    count++;
                }
            }
        }
        return count;
    }

    // credits 3.5% interest to every active SavingsAccount and logs each as a deposit transaction
    public int applyInterest(){
        Account[] accounts = accountManager.getAccounts();
        int count = 0;
        for (Account account : accounts) {
            if (account instanceof SavingsAccount sa && account.getStatus().equalsIgnoreCase("Active")) {
                double interest = sa.calculateInterest();
                Transaction transaction = account.deposit(interest);
                transactionManager.addTransaction(transaction);
                count++;
            }
        }
        return count;
    }


}



