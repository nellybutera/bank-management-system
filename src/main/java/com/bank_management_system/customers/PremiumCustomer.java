package com.bank_management_system.customers;

public class PremiumCustomer extends Customer {
    private static final double minimumBalance = 10000;

    public PremiumCustomer(String name, int age, String contact, String address){
        super(name, age, contact, address);
    }

    public double getMinimumBalance(){
        return minimumBalance;
    }

    public boolean hasWaivedFees(){
        return true;
    }

    @Override
    public void displayCustomerDetails(){
        System.out.println(" Customer ID : " + getCustomerId());
        System.out.println(" Name        : " + getName());
        System.out.println(" Type        : " + getCustomerType());
        System.out.println(" Age         : " + getAge());
        System.out.println(" Contact     : " + getContact());
        System.out.println(" Address     : " + getAddress());
    }

    @Override
    public String getCustomerType(){
        return "Premium";
    }
    
}
