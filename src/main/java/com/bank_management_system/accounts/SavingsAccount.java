package com.bank_management_system.accounts;
import com.bank_management_system.customers.Customer;
import com.bank_management_system.shared.InsufficientFundsException;

public class SavingsAccount extends Account{

    private final double interestRate;
    public static final double minimumBalance = 500.0;

    public SavingsAccount(Customer customer, double balance){
        super(customer, balance);
        interestRate = 0.035;
    }

    public double calculateInterest(){
        return getBalance() + interestRate;
    }

    public double getInterestRate(){ return interestRate; }
    public static double getMinimumBalance(){ return minimumBalance; }

    @Override
    public void displayAccountDetails() {
        System.out.printf("  %-8s | %-20s | Savings  | $%,12.2f | %s%n",
                getAccountNumber(), getCustomerName(), getBalance(), getStatus());
        System.out.printf("    Interest Rate: %.1f%% | Min Balance: $%.2f%n",
                interestRate * 100, minimumBalance);
    }

    @Override
    public String getAccountType(){
        return "Savings";
    }

    @Override
    protected void validateWithdrawal(double amount){
        if (getBalance() - amount < minimumBalance){
            throw new InsufficientFundsException(String.format(
                    "Withdrawal denied. Savings account must maintain a minimum balance of $%.2f. " +
                    "Current balance: $%.2f, requested: $%.2f.",
                    minimumBalance, getBalance(), amount));
        }
    }

    
}
