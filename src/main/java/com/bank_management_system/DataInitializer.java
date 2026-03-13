package com.bank_management_system;

import com.bank_management_system.accounts.AccountService;
import com.bank_management_system.customers.CustomerService;
import com.bank_management_system.customers.PremiumCustomer;
import com.bank_management_system.customers.RegularCustomer;

/**
 * Static utility class responsible for seeding the system with sample data.
 */
public class DataInitializer {

    private DataInitializer() {}

    // sample data
    // sample data

    public static void initializeSampleData(AccountService accountService, CustomerService customerService) {
        // customerService.register already adds each customer to the bank
        RegularCustomer c1 = customerService.registerRegularCustomer("John Smith",    35, "555-1234", "123 Main St, Springfield");
        RegularCustomer c2 = customerService.registerRegularCustomer("Sarah Johnson", 28, "555-2345", "456 Oak Ave, Riverside");
        PremiumCustomer c3 = customerService.registerPremiumCustomer("Michael Chen",  42, "555-3456", "789 Pine Rd, Lakewood");
        RegularCustomer c4 = customerService.registerRegularCustomer("Emily Brown",   31, "555-4567", "321 Elm St, Hillside");
        RegularCustomer c5 = customerService.registerRegularCustomer("David Wilson",  55, "555-5678", "654 Maple Dr, Westfield");

        accountService.createSavingsAccount(c1.getCustomerId(),  5_250.00);
        accountService.createSavingsAccount(c2.getCustomerId(), 3_450.00);
        accountService.createCheckingAccount(c3.getCustomerId(), 15_750.00);
        accountService.createSavingsAccount(c3.getCustomerId(),  12_000.00);  // second account for Michael Chen
        accountService.createCheckingAccount(c4.getCustomerId(),   890.00);
        accountService.createSavingsAccount(c5.getCustomerId(), 25_300.00);
    }
}
