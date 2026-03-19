package com.bank_management_system.customers;

import java.util.Collection;

import com.bank_management_system.bank.Bank;
import com.bank_management_system.exceptions.InputValidator;
import com.bank_management_system.exceptions.InvalidAmountException;

public class CustomerService {
    private final Bank bank;

    public CustomerService(Bank bank){
        this.bank = bank;
    }

    /**
     * Registers a new Regular customer and adds them to the bank.
     *
     * @param name    the customer's full name
     * @param age     the customer's age
     * @param contact the customer's phone number
     * @param address the customer's home address
     * @return the newly created RegularCustomer
     */
    public RegularCustomer registerRegularCustomer(String name, int age, String contact, String address) {
        validateInput(name, age, contact, address);
        RegularCustomer customer = new RegularCustomer(name.trim(), age, contact.trim(), address.trim());
        bank.addCustomer(customer);
        return customer;
    }

    /**
     * Registers a new Premium customer and adds them to the bank.
     *
     * @param name    the customer's full name
     * @param age     the customer's age
     * @param contact the customer's phone number
     * @param address the customer's home address
     * @return the newly created PremiumCustomer
     */
    public PremiumCustomer registerPremiumCustomer(String name, int age, String contact, String address) {
        validateInput(name, age, contact, address);
        PremiumCustomer customer = new PremiumCustomer(name.trim(), age, contact.trim(), address.trim());
        bank.addCustomer(customer);
        return customer;
    }

    /**
     * Returns all customers currently registered with the bank.
     *
     * @return an unmodifiable collection of all customers
     */
    public Collection<Customer> getAllCustomers() {
        return bank.getAllCustomers();
    }

    private void validateInput(String name, int age, String contact, String address){
        InputValidator.validateName(name);
        InputValidator.validateAge(age);
        InputValidator.validateContact(contact);
        InputValidator.validateAddress(address);
    }
}
