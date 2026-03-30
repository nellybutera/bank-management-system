package com.bank_management_system.customers;

public class RegularCustomer extends Customer {

    public RegularCustomer(String name, int age, String contact, String address) {
        super(name, age, contact, address);
    }

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
     * @return "Regular"
     */
    @Override
    public String getCustomerType() { return "Regular"; }
}
