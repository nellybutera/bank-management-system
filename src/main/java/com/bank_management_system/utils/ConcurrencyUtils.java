package com.bank_management_system.utils;

import com.bank_management_system.accounts.Account;

import java.util.Collection;
import java.util.List;

public class ConcurrencyUtils {

    private ConcurrencyUtils() {}

    /**
     * Runs a concurrent transaction simulation on the given account.
     * Five named threads execute deposits and withdrawals simultaneously,
     * demonstrating that synchronized balance updates produce a correct final balance.
     *
     * @param account the account to simulate transactions on
     */
    public static void runSimulation(Account account) {
        System.out.println("Running concurrent transaction simulation...\n");

        double startingBalance = account.getBalance();

        List<Thread> threads = List.of(
                makeThread("Thread-1", "DEPOSIT",    500.00, account),
                makeThread("Thread-2", "DEPOSIT",    300.00, account),
                makeThread("Thread-3", "WITHDRAWAL", 200.00, account),
                makeThread("Thread-4", "DEPOSIT",    150.00, account),
                makeThread("Thread-5", "WITHDRAWAL", 100.00, account)
        );

        threads.forEach(Thread::start);

        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        double expectedBalance = startingBalance + 500.00 + 300.00 - 200.00 + 150.00 - 100.00;

        System.out.println();
        System.out.println("\u2713 Thread-safe operations completed successfully.");
        System.out.printf("Starting Balance : $%,.2f%n", startingBalance);
        System.out.printf("Final Balance    : $%,.2f%n", account.getBalance());
        System.out.printf("Expected Balance : $%,.2f%n", expectedBalance);
        System.out.printf("Correct          : %s%n",
                Math.abs(account.getBalance() - expectedBalance) < 0.001 ? "YES \u2713" : "NO \u2717 (race condition detected)");
    }

    /**
     * Applies a $50.00 batch deposit to every active account using a parallel stream.
     * Each element is processed by a thread from the common fork-join pool, demonstrating
     * parallel stream processing across multiple accounts simultaneously.
     *
     * @param accounts all accounts in the system
     */
    public static void runParallelBatchSimulation(Collection<Account> accounts) {
        System.out.println("\nRunning parallel stream batch simulation...\n");

        accounts.parallelStream()
                .filter(account -> account.getStatus().equalsIgnoreCase("Active"))
                .forEach(account -> {
                    System.out.printf("[%s] Applying $50.00 batch deposit to %s (%s)%n",
                            Thread.currentThread().getName(),
                            account.getAccountNumber(),
                            account.getCustomerName());
                    account.deposit(50.00);
                });

        System.out.println("\n\u2713 Parallel batch simulation completed.");
    }

    private static Thread makeThread(String name, String type, double amount, Account account) {
        return new Thread(() -> {
            try {
                Thread.sleep((long) (Math.random() * 100));
                if (type.equals("DEPOSIT")) {
                    System.out.printf("%s: Depositing $%,.2f to %s%n",
                            name, amount, account.getAccountNumber());
                    account.deposit(amount);
                } else {
                    System.out.printf("%s: Withdrawing $%,.2f from %s%n",
                            name, amount, account.getAccountNumber());
                    account.withdraw(amount);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.out.printf("%s: Transaction failed — %s%n", name, e.getMessage());
            }
        }, name);
    }
}
