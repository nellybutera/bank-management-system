package com.bank_management_system;

import com.bank_management_system.shared.IllegalArgumentException;
import com.bank_management_system.shared.InputValidator;

import java.util.Scanner;

/**
 * Utility class that encapsulates all console input reading and validation.
 */
public class InputReader {

    private final Scanner scanner;

    public InputReader() {
        this.scanner = new Scanner(System.in);
    }

    /** Reads a raw line from the console, trimmed. */
    public String nextLine() {
        return scanner.nextLine().trim();
    }

    // Reads and validates a menu choice in [min, max]. Loops until valid.
    public int readMenuChoice(int min, int max) {
        while (true) {
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                InputValidator.validateMenuChoice(choice, min, max);
                return choice;
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            } catch (IllegalArgumentException e) {
                System.out.print(e.getMessage() + " Try again: ");
            }
        }
    }

    // Reads a positive decimal number. Loops until a parseable value is entered.
    public double readAmount() {
        while (true) {
            try {
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value <= 0) {
                    System.out.print("Amount must be greater than zero. Try again: $");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: $");
            }
        }
    }

    // Reads a positive integer for a given field label. Loops until valid.
    public int readPositiveInt(String label) {
        while (true) {
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value <= 0) {
                    System.out.print("Please enter a positive number for " + label + ": ");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number for " + label + ": ");
            }
        }
    }

    // Pauses and waits for the user to press Enter before returning to the menu.
    public void pressEnterToContinue() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public void close() {
        scanner.close();
    }
}
