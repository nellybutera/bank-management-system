package com.bank_management_system.bank;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.bank_management_system.customers.Customer;
import com.bank_management_system.exceptions.CustomerNotFoundException;

/**
 * Central registry that owns the collection of all customers in the system.
 * Accounts are managed separately by {@link com.bank_management_system.accounts.AccountManager};
 * Bank is responsible only for customer lookup and registration.
 */
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
     * @throws CustomerNotFoundException if no customer with the given ID exists
     */
    public Customer findCustomerById(String customerId) {
        Customer customer = customers.get(customerId);
        if (customer == null) {
            throw new CustomerNotFoundException("Customer not found: " + customerId);
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
