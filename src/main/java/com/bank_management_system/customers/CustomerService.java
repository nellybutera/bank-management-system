package com.bank_management_system.customers;

import java.util.Collection;

import com.bank_management_system.bank.Bank;
import com.bank_management_system.shared.InputValidator;
import com.bank_management_system.shared.InvalidAmountException;

public class CustomerService {
    private final Bank bank;

    public CustomerService(Bank bank){
        this.bank = bank;
    }

    public RegularCustomer registerRegularCustomer (String name, int age, String contact, String address){
        validateInput(name, age, contact, address);
        RegularCustomer customer = new RegularCustomer(name.trim(), age, contact.trim(), address.trim());
        bank.addCustomer(customer);
        return customer;
    }

    public PremiumCustomer registerPremiumCustomer(String name, int age, String contact, String address){
        validateInput(name, age, contact, address);
        PremiumCustomer customer = new PremiumCustomer(name.trim(), age, contact.trim(), address.trim());
        bank.addCustomer(customer);
        return customer;
    }

    public Collection<Customer> getAllCustomers(){
        return bank.getAllCustomers();
    }

    private void validateInput(String name, int age, String contact, String address){
        InputValidator.validateName(name);
        InputValidator.validateAge(age);
        InputValidator.validateContact(contact);
        InputValidator.validateAddress(address);
    }
}
