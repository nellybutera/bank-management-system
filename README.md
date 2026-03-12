# Bank Account Management System

A console-based Java application that simulates core banking operations — creating accounts, processing transactions, and viewing transaction history. Built to demonstrate Object-Oriented Programming principles and fundamental Data Structures & Algorithms concepts.

---

## What the Program Does

On startup the application pre-loads five sample customers and accounts. From the main menu, a user (bank staff) can:

1. **Create Account** — register a new Regular or Premium customer, choose a Savings or Checking account, and make an initial deposit
2. **View Accounts** — display all accounts in a formatted table showing balances, account type details, and a bank-wide total
3. **Process Transaction** — deposit or withdraw from any account with a confirmation preview before the transaction executes
4. **View Transaction History** — display all transactions for an account in reverse chronological order with a summary of deposits, withdrawals, and net change
5. **Exit** — close the application

---

## Setup Instructions

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Clone the Repository

```bash
git clone https://github.com/<your-username>/bank_management_system.git
cd bank_management_system
```

### Build the Project

```bash
mvn compile
```

### Run the Application

```bash
mvn exec:java -Dexec.mainClass="com.bank_management_system.Main"
```

Or compile and run manually:

```bash
javac -d target/classes src/main/java/com/bank_management_system/**/*.java
java -cp target/classes com.bank_management_system.Main
```

### Project Structure

```
src/main/java/com/bank_management_system/
├── Main.java
├── bank/
│   └── Bank.java
├── accounts/
│   ├── Account.java
│   ├── SavingsAccount.java
│   ├── CheckingAccount.java
│   ├── AccountManager.java
│   └── AccountService.java
├── customers/
│   ├── Customer.java
│   ├── RegularCustomer.java
│   ├── PremiumCustomer.java
│   └── CustomerService.java
├── transactions/
│   ├── Transaction.java
│   └── TransactionManager.java
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

## Class Breakdown

### `Main`
The entry point. Owns the top-level instances of every service and wires them together. Handles all console I/O through dedicated handler methods (`handleCreateAccount`, `handleProcessTransaction`, etc.) and input helpers (`readMenuChoice`, `readAmount`, `readPositiveInt`). No business logic lives here — it only delegates to the service layer.

### `Bank`
Stores all registered `Customer` objects in a `HashMap<String, Customer>` keyed by customer ID. Acts as the central customer registry. Provides `addCustomer`, `findCustomerById`, and `getAllCustomers`.

### `Account` *(abstract)*
The base class for all account types. Holds shared fields: `accountNumber`, `customer`, `balance`, and `status`. Implements `deposit()` and `withdraw()` as `final` methods so the transaction flow cannot be overridden — subclasses only control validation via the abstract `validateWithdrawal()` hook. Auto-generates account numbers (`ACC001`, `ACC002`, …) using a static counter.

### `SavingsAccount`
Extends `Account`. Enforces a minimum balance of $500 inside `validateWithdrawal()` — any withdrawal that would drop the balance below that threshold throws `InsufficientFundsException`. Has a fixed interest rate of 3.5% and exposes `calculateInterest()`.

### `CheckingAccount`
Extends `Account`. Allows overdraft up to $1,000 — `validateWithdrawal()` only blocks a withdrawal if it would exceed that overdraft limit. Has a $10 monthly fee that is automatically waived if the account was created for a `PremiumCustomer`. Exposes `applyMonthlyFee()` and `isFeesWaived()`.

### `AccountManager`
Manages a fixed-size array of up to 50 `Account` objects. Provides `addAccount`, `findAccount` (returns null if not found), `findAccountOrThrow` (throws `AccountNotFoundException`), `viewAllAccounts`, and `getTotalBalance`. All account lookups are done through linear search over the array.

### `AccountService`
The facade between `Main` and the account/transaction layer. Takes `Bank`, `AccountManager`, and `TransactionManager` through its constructor (composition). Every account operation — creation, deposit, withdrawal, history retrieval — goes through this class, keeping `Main` decoupled from internal details.

### `Customer` *(abstract)*
Base class for all customer types. Stores `customerId`, `name`, `age`, `contact`, `address`, and an `ArrayList<Account>` linking all accounts owned by this customer. Auto-generates customer IDs (`CUST001`, `CUST002`, …) via a static counter. Provides `viewCustomerAccounts()` and `calculateTotalAssets()`. Subclasses must implement `displayCustomerDetails()` and `getCustomerType()`.

### `RegularCustomer`
Extends `Customer`. Represents a standard customer with no special privileges. `isEligibleForFeeWaiver()` returns `false`.

### `PremiumCustomer`
Extends `Customer`. Represents a high-value customer with a minimum balance expectation of $10,000. Overrides `isEligibleForFeeWaiver()` to return `true`, which causes `AccountService` to automatically waive the monthly fee when creating a `CheckingAccount` for this customer.

### `CustomerService`
Handles customer registration. Validates that name, contact, and address are non-blank before creating a `RegularCustomer` or `PremiumCustomer` and registering them in the `Bank`.

### `Transaction`
An immutable snapshot of a single financial event. Stores `transactionId`, `accountNumber`, `type` (DEPOSIT / WITHDRAWAL), `amount`, `balanceAfter`, and a formatted `timestamp`. Auto-generates transaction IDs (`TXN001`, `TXN002`, …) via a static counter.

### `TransactionManager`
Manages a fixed-size array of up to 200 `Transaction` objects. Provides `addTransaction`, `viewTransactionsByAccount` (iterates in reverse for newest-first ordering), `calculateTotalDeposits`, and `calculateTotalWithdrawals`.

### `Transactable` *(interface)*
Defines the contract `boolean processTransaction(double amount, String type)`. Implemented by `Account`, ensuring any account type can be treated uniformly when processing transactions.

### Shared Exceptions
Custom `RuntimeException` subclasses for precise error signalling:
- `AccountNotFoundException` — account or customer ID does not exist
- `InsufficientFundsException` — balance rules violated (min balance or overdraft limit)
- `InvalidAmountException` — negative or zero amount supplied
- `IllegalArgumentException` — invalid input (blank fields, out-of-range menu choice)
- `IllegalStateException` — operation not valid for current account state (e.g. depositing into a closed account)

### `InputValidator`
A utility class with a private constructor (cannot be instantiated). Provides static validation methods used by both the service layer and `Main`: `validateName`, `validateAmount`, `validateId`, and `validateMenuChoice`.

---

## Object-Oriented Programming Concepts

### Encapsulation
Every class keeps its fields `private` and exposes only what is needed through `public` getters (and setters where mutation is allowed). For example, `Account.balance` has no setter — the only way to change it is through `deposit()` or `withdraw()`, which ensures the balance is always modified through validated, transaction-recorded paths.

### Inheritance
Two parallel hierarchies are used:
- **Account hierarchy** — `Account` (abstract) → `SavingsAccount` / `CheckingAccount`. Shared logic (deposit, withdraw, status checks) lives in the parent; type-specific rules (minimum balance, overdraft) live in the subclasses.
- **Customer hierarchy** — `Customer` (abstract) → `RegularCustomer` / `PremiumCustomer`. Shared identity and account linking lives in the parent; type-specific behaviour (`isEligibleForFeeWaiver`) lives in the subclass.

### Abstraction
`Account` and `Customer` are abstract classes — they cannot be instantiated directly. They define the *what* (the contract: `displayAccountDetails`, `validateWithdrawal`, `getCustomerType`) while leaving the *how* to each concrete subclass. This hides complexity and forces subclasses to provide their own meaningful implementations.

### Polymorphism
- `displayAccountDetails()` is overridden in both `SavingsAccount` and `CheckingAccount`. Calling it on any `Account` reference automatically produces the correct formatted output for that type.
- `validateWithdrawal()` is overridden in both subclasses. When `Account.withdraw()` calls `validateWithdrawal(amount)` internally, it triggers the correct rule — minimum balance check for savings, overdraft check for checking — without `Account` knowing which subtype it is.
- `isEligibleForFeeWaiver()` is overridden in `PremiumCustomer` to return `true`. `AccountService` calls this on any `Customer` reference, and the correct result is returned based on the actual type.
- `instanceof` pattern matching is used in `Main` to safely cast an `Account` to `SavingsAccount` or `CheckingAccount` when type-specific display details are needed.

### Composition
- `AccountManager` *has* an array of `Account` objects — it manages their storage and retrieval without inheriting from them.
- `TransactionManager` *has* an array of `Transaction` objects — same pattern.
- `AccountService` *has* a `Bank`, an `AccountManager`, and a `TransactionManager` injected through its constructor. This is **dependency injection via composition** — `AccountService` orchestrates the three without extending any of them.
- `Customer` *has* an `ArrayList<Account>` — linking a customer to all accounts they own.

### Interface
`Transactable` defines a single-method contract: `processTransaction(double amount, String type)`. `Account` implements it, meaning any account can be referenced as a `Transactable` and processed uniformly regardless of its concrete type.

### Static Members
Static counters (`accountCounter`, `customerCounter`, `transactionCounter`) in `Account`, `Customer`, and `Transaction` respectively auto-generate sequential IDs (`ACC001`, `CUST001`, `TXN001`) that are unique across the entire program lifetime without any external ID management.

---

## Data Structures & Algorithms Concepts

### Arrays (Fixed-Size)
`AccountManager` stores accounts in a fixed array of size 50. `TransactionManager` stores transactions in a fixed array of size 200. A separate integer counter (`accountCount`, `transactionCount`) tracks how many slots are in use, keeping `add` at O(1) for each insertion.

**Why arrays:** They provide direct index-based access, predictable memory allocation, and simple iteration — appropriate for a system with a known maximum capacity.

### Dynamic Array (ArrayList)
`Customer.accounts` uses an `ArrayList<Account>` because a single customer's account count is not known in advance and grows as accounts are created. Unlike the fixed bank-wide arrays, this needs to grow dynamically per customer.

**Why ArrayList:** Provides O(1) amortized append and O(n) iteration, with no need to pre-declare a maximum per customer.

### HashMap (Key-Value Lookup)
`Bank` stores customers in a `HashMap<String, Customer>` keyed by `customerId`. Looking up a customer by ID is O(1) average case.

**Why HashMap:** Customer lookup by ID is a frequent operation (every account creation and transaction goes through it). A HashMap avoids scanning the entire customer list each time.

### Linear Search
`AccountManager.findAccount()` and `TransactionManager.viewTransactionsByAccount()` both iterate through their arrays sequentially to find a match by account number. This is O(n) where n is the number of accounts or transactions.

**Why linear search:** The arrays are unsorted and small (max 50 accounts, max 200 transactions). For datasets of this size, linear search is simple and fast enough — a binary search would require keeping the array sorted, adding complexity without meaningful gain.

### Reverse Iteration (Newest-First Ordering)
`TransactionManager.viewTransactionsByAccount()` iterates the transaction array from the last index down to 0, printing the most recent transactions first. Since transactions are always appended in chronological order, reversing the iteration order is a O(n) way to display them newest-first without any sorting step.

**Time complexity:** O(n) — a single pass through the array in reverse. This is optimal since every transaction for the account must be visited at least once.

### Aggregation Pass
`TransactionManager.calculateTotalDeposits()` and `calculateTotalWithdrawals()` each perform a single O(n) pass over the transactions array, summing amounts that match the account number and type. These are used to produce the history summary (total deposits, total withdrawals, net change).
