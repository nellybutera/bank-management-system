package com.bank_management_system.customers;

import java.util.Collection;

import com.bank_management_system.bank.Bank;
import com.bank_management_system.shared.InvalidAmountException;

public class CustomerService {
    private final Bank bank;

    public CustomerService(Bank bank){
        this.bank = bank;
    }

    public RegularCustomer registerRegularCustomer (String name, int age, String contact, String address){
        validateInput(name, contact, address);
        RegularCustomer customer = new RegularCustomer(name.trim(), age, contact.trim(), address.trim());
        bank.addCustomer(customer);
        return customer;
    }

    public PremiumCustomer registerPremiumCustomer(String name, int age, String contact, String address){
        validateInput(name, contact, address);
        PremiumCustomer customer = new PremiumCustomer(name.trim(), age, contact.trim(), address.trim());
        bank.addCustomer(customer);
        return customer;
    }

    public Collection<Customer> getAllCustomers(){
        return bank.getAllCustomers();
    }

    private void validateInput(String name, String contact, String address){
        if (name == null || name.trim().isEmpty()){
            throw new InvalidAmountException("Customer name must not be blank");
        }
        if (contact == null || contact.trim().isEmpty()) {
            throw new InvalidAmountException("Customer contact must not be blank.");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new InvalidAmountException("Customer address must not be blank.");
        }
    }
}
