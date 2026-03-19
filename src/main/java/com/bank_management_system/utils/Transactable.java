package com.bank_management_system.utils;

public interface Transactable {
    boolean processTransaction(double amount, String type);
}
