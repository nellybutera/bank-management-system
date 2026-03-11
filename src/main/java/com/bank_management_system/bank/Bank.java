package com.bank_management_system.bank;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.bank_management_system.customers.Customer;
import com.bank_management_system.shared.AccountNotFoundException;

public class Bank {
    private final Map<String, Customer> customers = new HashMap<>();

    public void addCustomer(Customer customer){
        customers.put(customer.getCustomerId(), customer);
    }

    public Customer findCustomerById(String customerId) throws AccountNotFoundException {
        Customer customer = customers.get(customerId);
        if (customer == null) {
            throw new AccountNotFoundException("Customer not found with ID: " + customerId);
        }
        return customer;
    }

    public Collection<Customer> getAllCustomers() {
        return Collections.unmodifiableCollection(customers.values());
    }
}
