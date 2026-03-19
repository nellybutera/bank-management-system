package com.bank_management_system;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

/**
 * JUnit 5 extension that prints a formatted PASSED / FAILED line to the
 * console after each test method completes.
 *
 * Example output:
 *   Test: depositUpdatesBalance() ..................... PASSED
 *   Test: withdrawBelowMinimumThrowsException() ........ PASSED
 */
public class TestResultLogger implements TestWatcher {

    private static final int LINE_WIDTH = 65;

    @Override
    public void testSuccessful(ExtensionContext context) {
        print(context.getRequiredTestMethod().getName(), "PASSED");
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        print(context.getRequiredTestMethod().getName(),
              "FAILED  <- " + cause.getMessage());
    }

    private void print(String methodName, String result) {
        String label = "Test: " + methodName + "()";
        int dotCount = Math.max(1, LINE_WIDTH - label.length());
        System.out.println(label + " " + ".".repeat(dotCount) + " " + result);
    }
}
