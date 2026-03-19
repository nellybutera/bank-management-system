package com.bank_management_system.customers;

public class PremiumCustomer extends Customer {

    private static final double minimumBalance = 10000;

    public PremiumCustomer(String name, int age, String contact, String address) {
        super(name, age, contact, address);
    }

    /**
     * Returns the minimum balance required for a Premium account.
     *
     * @return the minimum balance amount
     */
    public double getMinimumBalance() { return minimumBalance; }

    /**
     * Premium customers are always eligible for a monthly fee waiver.
     *
     * @return true always
     */
    @Override
    public boolean isEligibleForFeeWaiver() { return true; }

    /**
     * Prints a formatted summary of this customer's profile details.
     */
    @Override
    public void displayCustomerDetails() {
        System.out.println(" Customer ID : " + getCustomerId());
        System.out.println(" Name        : " + getName());
        System.out.println(" Type        : " + getCustomerType());
        System.out.println(" Age         : " + getAge());
        System.out.println(" Contact     : " + getContact());
        System.out.println(" Address     : " + getAddress());
    }

    /**
     * Returns the customer tier label.
     *
     * @return "Premium"
     */
    @Override
    public String getCustomerType() { return "Premium"; }
}
