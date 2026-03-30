# Bank Account Management System

A console-based Java application that simulates core banking operations — creating accounts, processing transactions, and viewing transaction history. Built to demonstrate Object-Oriented Programming principles and fundamental Data Structures & Algorithms concepts.

---

## Documentation

| File | Audience | What it covers |
|---|---|---|
| `README.md` *(this file)* | Everyone | Setup, features, quick class map, OOP/DSA highlights |
| [`architecture.md`](architecture.md) | Developers | Full class responsibilities, sequence diagrams, SSOT design, DSA analysis |
| [`dummyguide.md`](dummyguide.md) | Beginners | Plain-English explanations of every concept using real-world analogies |

---

## Features

On startup the application pre-loads five sample customers and six accounts (Michael Chen has both a Checking and a Savings account). From the main menu a user (bank staff) can:

1. **Create Account** — open an account for an existing customer by ID, or register a brand-new Regular or Premium customer; choose a Savings or Checking account and set an initial deposit
2. **View Accounts** — display all accounts in a formatted table with balances and a bank-wide total
3. **Process Transaction** — deposit or withdraw from any account with a confirmation prompt before execution
4. **View Transaction History** — show all transactions for an account newest-first, with a deposit/withdrawal/net summary
5. **Close Account** — soft-delete an account (sets status to `Closed`; requires zero balance; preserves full transaction history)
6. **Apply Monthly Fees & Interest** — batch operation: deducts $10 from non-waived Checking accounts and credits 3.5% interest to active Savings accounts; all movements are logged to the ledger
7. **View Customer Accounts** — look up all accounts owned by a specific customer by ID
8. **Run Tests** — executes the full JUnit test suite via Maven and streams the formatted PASSED/FAILED results directly to the console
9. **Exit** — close the application

---

## Setup

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Clone & Run

```bash
git clone https://github.com/<your-username>/bank_management_system.git
cd bank_management_system
mvn compile
mvn exec:java -Dexec.mainClass="com.bank_management_system.Main"
```

Or without Maven:

```bash
javac -d target/classes src/main/java/com/bank_management_system/**/*.java
java -cp target/classes com.bank_management_system.Main
```

---

## Project Structure

```
src/main/java/com/bank_management_system/
├── Main.java                    Entry point — wires all dependencies
├── BankController.java          Menu loop and user interaction
├── InputReader.java             Validated console input
├── DataInitializer.java         Sample data on startup
├── bank/
│   └── Bank.java                Customer registry (HashMap)
├── accounts/
│   ├── Account.java             Abstract base — balance math and rules
│   ├── SavingsAccount.java      $500 minimum balance, 3.5% interest
│   ├── CheckingAccount.java     $1,000 overdraft, $10 monthly fee
│   ├── AccountManager.java      Account storage (fixed array, max 50)
│   └── AccountService.java      Central orchestrator for all account ops
├── customers/
│   ├── Customer.java            Abstract base — identity and account list
│   ├── RegularCustomer.java     Standard customer
│   ├── PremiumCustomer.java     Fee-exempt, $10,000 minimum expectation
│   └── CustomerService.java     Customer registration
├── transactions/
│   ├── Transaction.java         Immutable event record
│   └── TransactionManager.java  Ledger — Single Source of Truth (SSOT)
└── shared/
    ├── Transactable.java
    ├── InputValidator.java
    ├── AccountNotFoundException.java
    ├── InsufficientFundsException.java
    ├── OverdraftLimitExceededException.java
    ├── InvalidAmountException.java
    ├── IllegalArgumentException.java
    └── IllegalStateException.java
```

---

## OOP Concepts Applied

| Concept | Where |
|---|---|
| **Encapsulation** | `Account.balance` is `private` — only `deposit()` / `withdraw()` can change it, ensuring every mutation is validated and logged |
| **Inheritance** | `SavingsAccount` / `CheckingAccount` extend `Account`; `RegularCustomer` / `PremiumCustomer` extend `Customer` — shared logic in parent, type-specific rules in subclasses |
| **Abstraction** | `Account` and `Customer` are abstract — they define *what* must exist (`validateWithdrawal`, `getCustomerType`) without specifying *how* |
| **Polymorphism** | `account.validateWithdrawal(amount)` calls the correct subclass implementation at runtime — no `if/else` on type needed |
| **Composition** | `AccountService` holds `Bank` + `AccountManager` + `TransactionManager` via constructor injection, orchestrating them without inheriting from any |
| **Interface** | `Transactable` defines `processTransaction(double, String)` — `Account` implements it so any account can be treated uniformly |

---

## DSA Concepts Applied

| Concept | Where | Complexity |
|---|---|---|
| Fixed-size array | `AccountManager` (50 slots), `TransactionManager` (200 slots) | O(1) append |
| Dynamic array (ArrayList) | `Customer.accounts` — grows per customer | O(1) amortised append |
| Hash map | `Bank.customers` keyed by customer ID | O(1) average lookup |
| Linear search | `AccountManager.findAccountOrThrow()` | O(n) |
| Timestamp sort (newest first) | `TransactionManager.printTransactionTable()` — collects matching transactions into a `List`, sorts by `createdAt` descending via `Comparator.comparing(Transaction::getCreatedAt).reversed()` | O(n log n) |
| Filter + accumulate | `calculateTotalDeposits()` / `calculateTotalWithdrawals()` | O(n) |
| Guard clauses (fail-fast) | `Account.deposit()` / `withdraw()` — validate before mutating | O(1) checks |
| Static sequence generator | Auto-increment IDs in `Account`, `Customer`, `Transaction` | O(1) |

> Full DSA analysis with explanations is in [`architecture.md § 7`](architecture.md).

---

## Input Validation

All user input is validated before reaching the service layer via `InputValidator`. Invalid input throws `IllegalArgumentException` immediately and is caught by the controller — the menu loop always continues.

| Field | Rule |
|---|---|
| Customer / account name | Letters, spaces, hyphens, and apostrophes only — no digits or symbols |
| Age | Must be between 18 and 120 |
| Contact number | 7–15 characters; digits, spaces, `+`, `-`, `()` only |
| Address | Letters, digits, spaces, commas, hyphens — covers "123 Main St, Springfield" |
| Account number input | Must match `ACC` + digits (e.g. `ACC001`) |
| Customer ID input | Must match `CUST` + digits (e.g. `CUST001`) |
| Amount | Must be greater than zero |
| Menu choice | Must be within the displayed range |

---

## Custom Exceptions

| Exception | When it is thrown |
|---|---|
| `AccountNotFoundException` | Account or customer ID does not exist |
| `InsufficientFundsException` | Withdrawal would breach minimum balance or overdraft limit |
| `OverdraftLimitExceededException` | Withdrawal on a Checking account would exceed the $1,000 overdraft limit (extends `InsufficientFundsException`) |
| `InvalidAmountException` | Amount is zero or negative |
| `IllegalArgumentException` | Invalid input — blank field, bad format, or out-of-range menu choice |
| `IllegalStateException` | Operation attempted on a closed account |

---

## Unit Tests

17 tests across 3 files. Run with `mvn test` or via menu option 8 inside the application.

### AccountTest — 8 tests (JUnit 5, real objects)

| Test | Result |
|---|---|
| `depositUpdatesBalance` | PASSED |
| `depositZeroThrowsInvalidAmountException` | PASSED |
| `depositToClosedAccountThrowsIllegalStateException` | PASSED |
| `withdrawUpdatesBalance` | PASSED |
| `withdrawBelowMinimumThrowsException` | PASSED |
| `withdrawFromClosedAccountThrowsIllegalStateException` | PASSED |
| `overdraftWithinLimitAllowed` | PASSED |
| `overdraftExceedThrowsOverdraftLimitExceededException` | PASSED |

### TransactionManagerTest — 4 tests (JUnit 5, real objects)

| Test | Result |
|---|---|
| `addTransactionRecordsDeposit` | PASSED |
| `calculateTotalDepositsAggregatesMultiple` | PASSED |
| `depositsMinusWithdrawalsEqualsNetBalanceChange` | PASSED |
| `calculateTotalDepositsReturnsZeroForUnknownAccount` | PASSED |

### AccountServiceTest — 5 tests (JUnit 5 + Mockito)

| Test | Result |
|---|---|
| `depositLogsTransactionInLedger` | PASSED |
| `unknownAccountThrowsAccountNotFoundException` | PASSED |
| `createSavingsAccountBelowMinimumThrowsException` | PASSED |
| `createCheckingAccountWaivesFeeForPremiumCustomer` | PASSED |
| `closeAccountWithNonZeroBalanceThrowsIllegalStateException` | PASSED |

`AccountServiceTest` uses `@Mock` for `Bank`, `AccountManager`, and `TransactionManager` so that each test isolates `AccountService` from its dependencies. `AccountTest` and `TransactionManagerTest` use real instances — no mocks needed because those classes have no injected dependencies.

---

## Git Workflow

This project uses **feature branches** and **cherry-pick** to move tested changes selectively between branches.

### Branch structure

```bash
main                  # stable, production-ready code
feature/exceptions    # custom exception handling and input validation
feature/refactor      # clean code, Javadoc, and formatting standards
```

### Common commands used

```bash
# Create and switch to a new feature branch
git checkout -b feature/refactor

# Stage specific files for a focused commit
git add src/main/java/com/bank_management_system/transactions/TransactionManager.java

# Commit with a descriptive message
git commit -m "renamed constants to UPPER_SNAKE_CASE and broke down long methods into smaller ones"

# View commit history on the current branch
git log --oneline

# Cherry-pick a specific commit from another branch into the current one
git cherry-pick <commit-hash>

# Example: bring the exceptions commit from feature/exceptions into feature/refactor
git cherry-pick 58e273b

# Stash uncommitted changes before a cherry-pick to avoid conflicts
git stash
git cherry-pick <commit-hash>
git stash pop
```

### Why cherry-pick?

Instead of merging the entire `feature/exceptions` branch (which would bring in unrelated history), cherry-pick selectively applies only the one commit that added the input validators — keeping `feature/refactor` focused on its own purpose.

### Cherry-pick log

| Commit | From branch | To branch | What it brought | Why |
|---|---|---|---|---|
| `58e273b` | `feature/exceptions` | `feature/refactor` | Try-catch blocks in BankController, CustomerService; InputValidator with regex validators | Brought input validation into the refactor branch without merging unrelated exception history |
| `c392b8d` | `feature/refactor` | `feature/exceptions` | UPPER_SNAKE_CASE constants (`MAX_ACCOUNTS`, `MAX_TRANSACTIONS`), TransactionManager split into private helpers, `sumByTransactionType` DRY method | Tests need the refactored TransactionManager methods to exist on this branch |
| `4776cf3` | `feature/refactor` | `feature/exceptions` | Javadoc on Account, AccountService, Bank, Customer, CheckingAccount, SavingsAccount, InputReader, DataInitializer | Completes Javadoc coverage across all classes used in the test suite |
| `731b732` | `feature/refactor` | `feature/exceptions` | Javadoc on PremiumCustomer and RegularCustomer | Finishes the Javadoc pass for the full customer class hierarchy |
| `9c0ad63` | `feature/refactor` | `feature/exceptions` | BankController refactored into helper methods (≤25 lines each), `validateAccountNumber` / `validateCustomerId` calls added | Brings the full validation flow onto this branch before integration testing |
| `77a9103` | `feature/refactor` | `feature/exceptions` | README, architecture.md, dummyguide.md updated with refactoring and validation changes | Keeps documentation consistent across both branches at this point |
