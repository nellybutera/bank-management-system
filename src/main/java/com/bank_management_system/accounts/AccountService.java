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

    // additional method to close an account
    public void closeAccount(String accountNumber){
        Account account = accountManager.findAccountOrThrow(accountNumber);

        if(account.getBalance() != 0){
            throw new IllegalStateException("Withdraw all funds before closing.");
        }

        account.closeAccount();
        System.out.println("Account " + accountNumber + " is now closed");
    }


}



