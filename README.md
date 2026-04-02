# Bank Account Management System

A console-based Java application that simulates core banking operations — creating accounts, processing transactions, and viewing transaction history. Built to demonstrate Object-Oriented Programming principles and fundamental Data Structures & Algorithms concepts.

---

## Documentation

| File | Audience | What it covers |
|---|---|---|
| `README.md` *(this file)* | Everyone | Setup, features, OOP/DSA highlights, Collections & Functional Programming |
| [`docs/collections-architecture.md`](docs/collections-architecture.md) | Developers | Collection choices, stream patterns, reduce(), thread safety design, migration summary |
| [`docs/git-workflow.md`](docs/git-workflow.md) | Developers | Branch structure, commit conventions, cherry-pick log |
| [`architecture.md`](architecture.md) | Developers | Full class responsibilities, sequence diagrams, SSOT design |
| [`dummyguide.md`](dummyguide.md) | Beginners | Plain-English explanations of every concept using real-world analogies |

---

## Features

On startup the application loads saved data from `data/accounts.txt` and `data/transactions.txt` (created automatically on first save). If no save file exists, five sample customers and six accounts are pre-loaded. From the main menu a user (bank staff) can:

1. **Manage Accounts** — sub-menu grouping all account lifecycle operations:
   - Create Account — register a new or existing customer and open a Savings or Checking account
   - View All Accounts — formatted table of all accounts with balances and a bank-wide total
   - View Customer Accounts — look up all accounts owned by a customer ID
   - Close Account — soft-delete (requires zero balance; preserves full transaction history)
   - Apply Monthly Fees & Interest — deducts $10 from non-waived Checking accounts; credits 3.5% interest to active Savings accounts

2. **Perform Transactions** — deposit, withdrawal, or transfer between accounts with a confirmation prompt

3. **Account Statements** — sub-menu for reporting:
   - Generate Account Statement — formatted statement sorted newest-first, with totals and net change
   - View Transaction History — same data with choice of sort order (date or amount)

4. **Save Data** — writes all accounts and transactions to pipe-delimited flat files under `data/`

5. **Run Concurrent Simulation** — demonstrates thread safety; single-account multi-thread race or parallel-stream batch deposit across all accounts

6. **Run Tests** — executes the full JUnit test suite via Maven and streams PASSED/FAILED results to the console

7. **Exit** — auto-saves all data and closes the application

---

## Setup

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Clone & Run

```bash
git clone (https://github.com/nellybutera/bank-management-system)
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
├── Main.java                    Entry point — wires all dependencies, loads persisted data
├── BankController.java          Menu loop and user interaction
├── InputReader.java             Validated console input
├── DataInitializer.java         Sample data seeded on first run
├── bank/
│   └── Bank.java                Customer registry (HashMap)
├── accounts/
│   ├── Account.java             Abstract base — balance math, thread-safe updateBalance
│   ├── SavingsAccount.java      $500 minimum balance, 3.5% interest
│   ├── CheckingAccount.java     $1,000 overdraft, $10 monthly fee
│   ├── AccountManager.java      Account storage (LinkedHashMap — O(1) lookup by account number)
│   └── AccountService.java      Central orchestrator for all account ops
├── customers/
│   ├── Customer.java            Abstract base — identity and account list
│   ├── RegularCustomer.java     Standard customer
│   ├── PremiumCustomer.java     Fee-exempt, $10,000 minimum expectation
│   └── CustomerService.java     Customer registration with email validation
├── transactions/
│   ├── Transaction.java         Immutable event record with file serialisation
│   └── TransactionManager.java  Thread-safe ledger using synchronizedList
├── persistence/
│   └── FilePersistenceService.java  NIO stream-based save/load for accounts and transactions
├── utils/
│   ├── Transactable.java        Interface for deposit/withdraw operations
│   ├── ValidationUtils.java     Compiled regex Patterns and Predicate<String> validators
│   └── ConcurrencyUtils.java    Thread simulation and parallel-stream batch demo
└── exceptions/
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
| Linked hash map | `AccountManager.accounts` keyed by account number — insertion-order iteration, O(1) lookup | O(1) average |
| Dynamic array (ArrayList) | `TransactionManager` (synchronizedList), `Customer.accounts` | O(1) amortised append |
| Hash map | `Bank.customers` keyed by customer ID | O(1) average lookup |
| Hash map | `FilePersistenceService.customerCache` — deduplication during file load | O(1) average lookup |
| Map lookup | `AccountManager.findAccountOrThrow()` — direct `map.get()` | O(1) average |
| Timestamp sort (newest first) | `TransactionManager.printTransactionTable()` — `Comparator.comparing(Transaction::getCreatedAt).reversed()` | O(n log n) |
| Filter + accumulate | `calculateTotalDeposits()` / `calculateTotalWithdrawals()` using streams | O(n) |
| Stream pipeline | `FilePersistenceService.loadAccounts()` — `Files.lines().filter().map().collect()` | O(n) |
| Parallel stream | `ConcurrencyUtils.runParallelBatchSimulation()` — fork-join pool across all accounts | O(n/p) |
| Guard clauses (fail-fast) | `Account.deposit()` / `withdraw()` — validate before mutating | O(1) checks |
| Static sequence generator | Auto-increment IDs in `Account`, `Customer`, `Transaction` | O(1) |

> Full DSA analysis with explanations is in [`architecture.md § 7`](architecture.md).

---

## Collections & Functional Programming

### Collections in use

| Collection | Where | Why |
|---|---|---|
| `LinkedHashMap<String, Account>` | `AccountManager` | O(1) lookup by account number; insertion order preserved for consistent display |
| `HashMap<String, Customer>` | `Bank` | O(1) lookup by customer ID; order not needed |
| `synchronizedList(ArrayList<Transaction>)` | `TransactionManager` | Thread-safe unbounded ledger; index access not needed |
| `ArrayList<Account>` | `Customer.accounts` | Ordered list of accounts per customer; grows dynamically |
| `HashMap<String, Customer>` | `FilePersistenceService.customerCache` | Deduplication during file load — prevents creating two objects for the same customer ID |

### Functional programming patterns

**Lambdas** are used wherever an anonymous function is clearer than a named method:
```java
// Predicate lambda — filter active Checking accounts
Predicate<Account> activeChecking = account ->
    account instanceof CheckingAccount && account.getStatus().equalsIgnoreCase("Active");

// Runnable lambda — thread body
Runnable task = () -> { account.deposit(50); };
```

**Method references** replace lambdas where a named method already exists:
```java
fees.forEach(transactionManager::addTransaction);  // instance method reference
.map(Transaction::fromLine)                        // static method reference
.forEach(Transaction::displayTransactionDetails)   // instance method reference on parameter
accounts.forEach(accountManager::addAccount);      // instance method reference
```

**Stream pipelines** process collections without mutating them:
```java
// Load accounts from file — filter blank lines, map each to an object, collect
Files.lines(path).filter(s -> !s.isBlank()).map(this::parseAccount).collect(toList())

// Sort transactions newest-first
transactions.stream()
    .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
    .collect(toList())

// Group transactions by type for summary totals
transactions.stream().collect(Collectors.groupingBy(t -> t.getType().toUpperCase()))
```

**`reduce()`** accumulates transaction amounts into a total:
```java
// In FunctionalUtils.sumAmounts()
transactions.stream()
    .map(Transaction::getAmount)
    .reduce(0.0, Double::sum);   // identity element 0.0; accumulator Double::sum
```

**`Predicate<String>`** constants in `ValidationUtils` allow validation to be passed as data:
```java
if (ValidationUtils.isValidEmail.test(email)) { ... }
```

**Parallel stream** uses the fork-join pool for batch operations:
```java
accounts.parallelStream()
    .filter(a -> a.getStatus().equalsIgnoreCase("Active"))
    .forEach(account -> account.deposit(50));
```

See [`docs/collections-architecture.md`](docs/collections-architecture.md) for design rationale and trade-offs.

---

## Input Validation

All user input is validated before reaching the service layer via `InputValidator`. Invalid input throws `IllegalArgumentException` immediately and is caught by the controller — the menu loop always continues.

All patterns are compiled once as `static final Pattern` constants in `ValidationUtils` and exposed as `Predicate<String>` fields. `InputValidator` delegates to `ValidationUtils` so the regex lives in one place.

| Field | Rule |
|---|---|
| Customer / account name | Letters, spaces, hyphens, and apostrophes only — no digits or symbols |
| Age | Must be between 18 and 120 |
| Contact number | 7–15 characters; digits, spaces, `+`, `-`, `()` only |
| Email | Standard email format — `local@domain.tld` |
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

32 tests across 4 files. Run with `mvn test` or via menu option 9 inside the application.

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

### ExceptionTest — 12 tests (JUnit 5, real objects)

Dedicated file for exception class behaviour, `InputValidator` edge cases, and exception hierarchy contracts.

| Test | Result |
|---|---|
| `validateAmountZeroThrowsInvalidAmountException` | PASSED |
| `validateAmountNegativeThrowsInvalidAmountException` | PASSED |
| `invalidAmountExceptionMessageIsDescriptive` | PASSED |
| `withdrawBelowSavingsMinimumThrowsInsufficientFundsException` | PASSED |
| `insufficientFundsExceptionMessageContainsBalance` | PASSED |
| `overdraftExceededExtendsInsufficientFundsException` | PASSED |
| `overdraftExceededIsSpecificSubtype` | PASSED |
| `depositToClosedAccountThrowsIllegalStateException` | PASSED |
| `withdrawFromClosedAccountThrowsIllegalStateException` | PASSED |
| `accountNotFoundExceptionCarriesMessage` | PASSED |
| `menuChoiceBelowRangeThrowsIllegalArgumentException` | PASSED |
| `menuChoiceAboveRangeThrowsIllegalArgumentException` | PASSED |

### TransactionManagerTest — 4 tests (JUnit 5, real objects)

| Test | Result |
|---|---|
| `addTransactionRecordsDeposit` | PASSED |
| `calculateTotalDepositsAggregatesMultiple` | PASSED |
| `depositsMinusWithdrawalsEqualsNetBalanceChange` | PASSED |
| `calculateTotalDepositsReturnsZeroForUnknownAccount` | PASSED |

### AccountServiceTest — 8 tests (JUnit 5 + Mockito)

| Test | Result |
|---|---|
| `depositLogsTransactionInLedger` | PASSED |
| `unknownAccountThrowsAccountNotFoundException` | PASSED |
| `createSavingsAccountBelowMinimumThrowsException` | PASSED |
| `createCheckingAccountWaivesFeeForPremiumCustomer` | PASSED |
| `closeAccountWithNonZeroBalanceThrowsIllegalStateException` | PASSED |
| `transferUpdatesBothAccountBalances` | PASSED |
| `transferLogsTwoTransactions` | PASSED |
| `transferToSameAccountThrowsIllegalArgumentException` | PASSED |

`AccountServiceTest` uses `@Mock` for `Bank`, `AccountManager`, and `TransactionManager` so that each test isolates `AccountService` from its dependencies. `AccountTest`, `ExceptionTest`, and `TransactionManagerTest` use real instances — no mocks needed because those classes have no injected dependencies.

---

## File Persistence

On startup `Main` calls `FilePersistenceService.loadAccounts()` and `loadTransactions()`. These use `Files.lines()` to stream pipe-delimited records from `data/accounts.txt` and `data/transactions.txt`, mapping each line through a method reference (`this::parseAccount`, `Transaction::fromLine`) and collecting into `ArrayList`. If the files are empty or absent, `DataInitializer` seeds the default sample data instead.

Accounts and transactions expose `toFileLine()` for serialisation. Restoration constructors on `Account`, `Customer`, and `Transaction` accept an explicit ID so the static auto-increment counter is not touched during loading. After loading, `resetCounters()` parses the highest numeric suffix from the loaded IDs and calls `Account.resetCounter()`, `Customer.resetCounter()`, and `Transaction.resetCounter()` so subsequent creates resume from the correct next ID.

---

## Concurrency

Thread safety is layered at two levels:

- **`Account.updateBalance(double)`** — `synchronized` instance method called by both `deposit()` and `withdraw()`, preventing interleaved balance mutations from concurrent threads
- **`TransactionManager`** — the internal `ArrayList` is wrapped in `Collections.synchronizedList()` for safe individual list operations, and `addTransaction()` is additionally `synchronized` to guard the compound read-then-write operation

`ConcurrencyUtils` provides two demos accessible from menu option 11:
1. **Single-account thread simulation** — 5 named threads each deposit/withdraw concurrently, then the final balance is compared against the expected value
2. **Parallel-stream batch** — `parallelStream()` over all active accounts applies a $50 deposit using the fork-join pool; thread names are printed to show parallelism

---

## Git Workflow

See [`docs/git-workflow.md`](docs/git-workflow.md) for the full branch structure, commit conventions, common commands, and cherry-pick log.

### Branch overview

```bash
main                                        # stable, production-ready
feature/refactor                            # clean code, Javadoc, naming standards
feature/exceptions                          # custom exception handling and validation
feature/testing                             # JUnit 5 test suite
feature/collections                         # Java Collections API migration
feature/file-persistence                    # NIO file I/O with functional streams
feature/regex-validation                    # compiled-regex input validation
feature/concurrency                         # thread safety and concurrent simulation
feature/clean-code-and-missing-lab2-functionality  # ExceptionTest, docs/, README update
```
