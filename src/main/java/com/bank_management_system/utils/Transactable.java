package com.bank_management_system.utils;

/**
 * Contract for objects that can process financial transactions.
 * Implemented by {@link com.bank_management_system.accounts.Account}.
 */
public interface Transactable {

    /**
     * Applies a financial transaction to this object.
     *
     * @param amount the transaction amount (must be positive)
     * @param type   the transaction type — "DEPOSIT" or "WITHDRAWAL"
     * @return true if the transaction was applied successfully
     * @throws com.bank_management_system.exceptions.InvalidAmountException   if the amount is zero or negative
     * @throws com.bank_management_system.exceptions.InsufficientFundsException if a withdrawal cannot be fulfilled
     * @throws com.bank_management_system.exceptions.IllegalArgumentException  if the type is unrecognised
     */
    boolean processTransaction(double amount, String type);
}
