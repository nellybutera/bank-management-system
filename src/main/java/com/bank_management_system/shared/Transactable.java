package com.bank_management_system.shared;

public interface Transactable {
    boolean processTransaction(double amount, String type);
}
