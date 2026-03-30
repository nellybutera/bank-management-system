package com.bank_management_system;

import java.util.Scanner;

import com.bank_management_system.exceptions.IllegalArgumentException;
import com.bank_management_system.utils.InputValidator;

/**
 * Utility class that encapsulates all console input reading and validation.
 */
public class InputReader {

    private final Scanner scanner;

    public InputReader() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Reads a raw line from the console and trims leading/trailing whitespace.
     *
     * @return the trimmed input string
     */
    public String nextLine() {
        return scanner.nextLine().trim();
    }

    /**
     * Reads and validates a menu choice in the range [min, max].
     * Loops until the user enters a valid integer within the range.
     *
     * @param min the minimum valid option
     * @param max the maximum valid option
     * @return the validated menu choice
     */
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

    /**
     * Reads a positive decimal amount from the console.
     * Loops until the user enters a valid number greater than zero.
     *
     * @return the validated positive amount
     */
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

    /**
     * Reads a positive integer for the given field label.
     * Loops until the user enters a valid positive integer.
     *
     * @param label a descriptive name for the field, used in error messages (e.g. "age")
     * @return the validated positive integer
     */
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

    /**
     * Pauses execution and waits for the user to press Enter before returning to the menu.
     */
    public void pressEnterToContinue() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Closes the underlying Scanner and releases the input stream.
     */
    public void close() {
        scanner.close();
    }
}
