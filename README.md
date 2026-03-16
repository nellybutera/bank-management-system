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
8. **Exit** — close the application

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
| Reverse traversal | `TransactionManager.viewTransactionsByAccount()` — newest-first display | O(n) |
| Filter + accumulate | `calculateTotalDeposits()` / `calculateTotalWithdrawals()` | O(n) |
| Guard clauses (fail-fast) | `Account.deposit()` / `withdraw()` — validate before mutating | O(1) checks |
| Static sequence generator | Auto-increment IDs in `Account`, `Customer`, `Transaction` | O(1) |

> Full DSA analysis with explanations is in [`architecture.md § 7`](architecture.md).

---

## Custom Exceptions

| Exception | When it is thrown |
|---|---|
| `AccountNotFoundException` | Account or customer ID does not exist |
| `InsufficientFundsException` | Withdrawal would breach minimum balance or overdraft limit |
| `InvalidAmountException` | Amount is zero or negative |
| `IllegalArgumentException` | Blank input field or out-of-range menu choice |
| `IllegalStateException` | Operation attempted on a closed account |
