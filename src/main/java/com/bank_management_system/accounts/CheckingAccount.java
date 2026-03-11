package com.bank_management_system.accounts;

import com.bank_management_system.customers.Customer;
import com.bank_management_system.shared.InsufficientFundsException;

public class CheckingAccount extends Account{
    private double overdraftLimit;
    private double monthlyFee;

    private final boolean feesWaived;

    public CheckingAccount(Customer customer, double balance, boolean feesWaived){
        super( customer, balance);
        overdraftLimit = 1000;
        monthlyFee = 10;

        this.feesWaived = feesWaived;
    }

    @Override
    public String getAccountType(){
        return "Checking";
    }

    public void applyMonthlyFee(){
        if (!feesWaived){
            withdraw(monthlyFee);
        }
    }


    @Override
    protected void validateWithdrawal(double amount) throws InsufficientFundsException{
        if (getBalance() - amount < (- overdraftLimit)){
            throw new InsufficientFundsException(String.format(
                    "Withdrawal denied. Checking account has exceeded overdraft limit of $%.2f. %n " +
                    "Current balance: $%.2f, requested: $%.2f.",
                    overdraftLimit, getBalance(), amount));
        }
    }

    @Override
    public void displayAccountDetails() {
        System.out.printf("  %-8s | %-20s | Checking | $%,12.2f | %s%n",
                getAccountNumber(), getCustomerName(), getBalance(), getStatus());
        System.out.printf("    Overdraft Limit: $%,.2f | Monthly Fee: $%.2f%s%n",
                overdraftLimit, monthlyFee, feesWaived ? " (Waived)" : "");
    }

    
    
}
