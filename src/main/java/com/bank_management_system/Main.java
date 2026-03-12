package com.bank_management_system;

import com.bank_management_system.accounts.AccountManager;
import com.bank_management_system.accounts.AccountService;
import com.bank_management_system.bank.Bank;
import com.bank_management_system.customers.CustomerService;
import com.bank_management_system.transactions.TransactionManager;

public class Main {

    public static void main(String[] args) {
        Bank               bank               = new Bank();
        AccountManager     accountManager     = new AccountManager();
        TransactionManager transactionManager = new TransactionManager();
        AccountService     accountService     = new AccountService(bank, accountManager, transactionManager);
        CustomerService    customerService    = new CustomerService(bank);
        InputReader        inputReader        = new InputReader();

        DataInitializer.initializeSampleData(accountService, customerService);

        BankController controller = new BankController(accountService, customerService, inputReader);
        controller.start();
    }
}
