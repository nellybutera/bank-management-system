package com.bank_management_system.bank;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.bank_management_system.customers.Customer;
import com.bank_management_system.shared.AccountNotFoundException;

public class Bank {
    private final Map<String, Customer> customers = new HashMap<>();

    /**
     * Registers a customer in the bank's internal store.
     *
     * @param customer the customer to add
     */
    public void addCustomer(Customer customer) {
        customers.put(customer.getCustomerId(), customer);
    }

    /**
     * Finds and returns a customer by their ID.
     *
     * @param customerId the ID to search for
     * @return the matching Customer
     * @throws AccountNotFoundException if no customer with the given ID exists
     */
    public Customer findCustomerById(String customerId) throws AccountNotFoundException {
        Customer customer = customers.get(customerId);
        if (customer == null) {
            throw new AccountNotFoundException("Customer not found with ID: " + customerId);
        }
        return customer;
    }

    /**
     * Returns all customers registered with the bank.
     *
     * @return an unmodifiable collection of all customers
     */
    public Collection<Customer> getAllCustomers() {
        return Collections.unmodifiableCollection(customers.values());
    }
}
