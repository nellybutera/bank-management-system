package com.bank_management_system.customers;

/**
 * A premium-tier customer who enjoys enhanced benefits:
 * monthly fee waiver on Checking accounts and a minimum required balance of $10,000.
 */
public class PremiumCustomer extends Customer {

    private static final double MINIMUM_BALANCE = 10000;

    public PremiumCustomer(String name, int age, String contact, String email, String address) {
        super(name, age, contact, email, address);
    }

    /** Restoration constructor — rebuilds from file data with an explicit customer ID. */
    public PremiumCustomer(String customerId, String name, int age, String contact, String email, String address) {
        super(customerId, name, age, contact, email, address);
    }

    /**
     * Returns the minimum balance required for a Premium account.
     *
     * @return the minimum balance amount
     */
    public double getMinimumBalance() { return MINIMUM_BALANCE; }

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
