
# Bank Management System — Architecture Documentation

---

## 1. Architecture Overview

The system follows a **layered Service Layer pattern** with a clear separation of concerns across four tiers:

```
┌─────────────────────────────────────┐
│          Presentation Layer         │  BankController, InputReader
├─────────────────────────────────────┤
│           Service Layer             │  AccountService, CustomerService
├─────────────────────────────────────┤
│           Manager Layer             │  AccountManager, TransactionManager, Bank
├─────────────────────────────────────┤
│            Domain Layer             │  Account, Customer, Transaction (+ subclasses)
└─────────────────────────────────────┘
```

**Key principles applied:**

- **Single Responsibility:** Each class owns exactly one concern (e.g. `TransactionManager` only stores and queries transactions; it never touches account balances).
- **Abstract Base Classes:** `Account` and `Customer` are abstract — concrete behaviour lives in `SavingsAccount`/`CheckingAccount` and `RegularCustomer`/`PremiumCustomer`.
- **Dependency Injection:** `AccountService` receives `Bank`, `AccountManager`, and `TransactionManager` through its constructor, keeping it testable and loosely coupled.
- **Single Source of Truth (SSOT):** `TransactionManager` is the authoritative ledger; every state-changing financial operation is recorded there.

---

## 2. Class Responsibilities

### Domain Layer

| Class | Type | Responsibility |
|---|---|---|
| `Account` | Abstract class | Holds balance, owns the math for `deposit()` / `withdraw()`, enforces closed-account rules, creates `Transaction` objects after each operation. Implements `Transactable`. |
| `SavingsAccount` | Concrete (`extends Account`) | Adds a 3.5 % interest rate and a $500 minimum-balance constraint enforced in `validateWithdrawal()`. `calculateInterest()` returns `balance * interestRate` (the interest amount earned). |
| `CheckingAccount` | Concrete (`extends Account`) | Adds a $1,000 overdraft limit and a $10 monthly fee that is automatically waived for Premium customers. `applyMonthlyFee()` returns a `Transaction` (or `null` if waived) so `AccountService` can log it. |
| `Customer` | Abstract class | Stores identity fields (name, age, contact, address), owns a `List<Account>`, and exposes `addAccount()` / `getAccounts()`. |
| `RegularCustomer` | Concrete (`extends Customer`) | No fee waiver; `isEligibleForFeeWaiver()` returns `false`. |
| `PremiumCustomer` | Concrete (`extends Customer`) | `isEligibleForFeeWaiver()` returns `true`, triggering fee-free `CheckingAccount` creation. |
| `Transaction` | Data/record class | Immutable snapshot of one financial event: account number, type (`DEPOSIT`/`WITHDRAWAL`), amount, post-transaction balance, and an auto-generated timestamp. |

### Manager Layer

| Class | Type | Responsibility |
|---|---|---|
| `Bank` | Repository | `HashMap<String, Customer>` — the master customer registry; provides O(1) lookup by customer ID. |
| `AccountManager` | Repository | Fixed array of up to `MAX_ACCOUNTS` (50) `Account` objects; provides `findAccountOrThrow()` (throws `AccountNotFoundException` if not found) and `getAccounts()` (returns a copy of the used slice for iteration). |
| `TransactionManager` | Ledger / SSOT | Fixed array of up to `MAX_TRANSACTIONS` (200) `Transaction` records; appends every financial event. `viewTransactionsByAccount()` is broken into four private helpers — `countMatchingTransactions()`, `printTransactionTable()`, `printTransactionSummary()`, and `sumByTransactionType()` — keeping each method under 25 lines. `calculateTotalDeposits()` and `calculateTotalWithdrawals()` both delegate to `sumByTransactionType()` to eliminate duplicate logic. |

### Service Layer

| Class | Type | Responsibility |
|---|---|---|
| `AccountService` | Orchestrator | The single entry point for all account and financial operations. Coordinates `Bank`, `AccountManager`, and `TransactionManager`. Exposes: account creation, `processTransaction()` (unified deposit/withdrawal entry point), close account (soft delete), `applyMonthlyFees()` (batch fee deduction for all non-waived checking accounts), and `applyInterest()` (batch interest credit for all active savings accounts). |
| `CustomerService` | Orchestrator | Validates and creates `RegularCustomer` / `PremiumCustomer` objects, then registers them in `Bank`. Calls `InputValidator` to enforce all four field rules — name format, age range (18–120), contact number format, and address format — before any object is created. |

### Presentation / I/O Layer

| Class | Type | Responsibility |
|---|---|---|
| `BankController` | Controller | Main menu loop (8 options); translates raw user choices into service calls; catches and displays domain exceptions. Handles: create, view, transact, history, close account, apply fees/interest, view customer accounts, exit. Long handlers (`handleProcessTransaction`, `handleCreateAccount`) are broken into private helper methods (`printAccountSummary`, `readTransactionType`, `readTransactionAmount`, `confirmTransaction`, `lookUpExistingCustomer`, `registerNewCustomer`, `openAccount`, `printAccountCreatedConfirmation`) to keep every method under 25 lines. Calls `InputValidator.validateAccountNumber()` and `validateCustomerId()` before passing IDs to the service layer. |
| `InputReader` | I/O helper | Wraps `Scanner`; provides validated reads for strings, menu choices, amounts, and positive integers with automatic re-prompt on invalid input. |
| `DataInitializer` | Bootstrap utility | Populates the system with 5 sample customers and 6 sample accounts on startup (Michael Chen holds two accounts to demonstrate multi-account customers). |
| `Main` | Entry point | Wires all dependencies together (manual DI) and launches `BankController.start()`. |

### Shared Utilities

| Class / Interface | Responsibility |
|---|---|
| `Transactable` (interface) | Contract (`processTransaction(double, String)`) that `Account` fulfils. |
| `InputValidator` | Static validation helpers. Provides eight validators: `validateName` (letters/spaces/hyphens/apostrophes only), `validateAge` (18–120), `validateContact` (7–15 digit phone format), `validateAddress` (letters, digits, spaces, commas, hyphens), `validateAccountNumber` (`ACC` + digits), `validateCustomerId` (`CUST` + digits), `validateAmount` (must be > 0), and `validateMenuChoice` (within displayed range). All throw `IllegalArgumentException` on failure. |
| `InvalidAmountException` | Thrown for negative or zero monetary values. |
| `InsufficientFundsException` | Thrown when a withdrawal would breach the minimum balance or overdraft limit. |
| `AccountNotFoundException` | Thrown when a lookup by account / customer ID fails. |
| `IllegalArgumentException` | Thrown when input fails format or range validation (blank field, wrong pattern, out-of-range menu choice). |
| `IllegalStateException` | Thrown when an operation is attempted on a closed account. |
| `OverdraftLimitExceededException` | Thrown by `CheckingAccount.validateWithdrawal()` when a withdrawal would push the balance below `-overdraftLimit`. Extends `InsufficientFundsException` — existing catch blocks for the parent type still handle it automatically. |

---

## 3. Dependency Graph

```
Main
 ├─ Bank
 ├─ AccountManager
 ├─ TransactionManager
 ├─ AccountService ──────── Bank, AccountManager, TransactionManager
 ├─ CustomerService ──────── Bank
 ├─ InputReader
 └─ BankController ───────── AccountService, CustomerService, InputReader

Account (abstract) ──── implements Transactable
 ├─ references Customer
 ├─ creates Transaction
 ├─ SavingsAccount
 └─ CheckingAccount

Customer (abstract)
 ├─ owns List<Account>
 ├─ RegularCustomer
 └─ PremiumCustomer
```

---

## 4. Sequence Flows

### 4.1 Application Startup

```
Main.main()
  │
  ├─ new Bank()
  ├─ new AccountManager()          ← capacity: 50 accounts
  ├─ new TransactionManager()      ← capacity: 200 transactions
  ├─ new AccountService(bank, accountManager, transactionManager)
  ├─ new CustomerService(bank)
  ├─ new InputReader()
  ├─ DataInitializer.initializeSampleData(accountService, customerService)
  │     └─ creates 5 customers + 6 accounts via the service layer
  └─ new BankController(...).start()   ← enters the menu loop
```

---

### 4.2 Account Creation Flow

**User selects menu option 1 — Create Account.**

```
BankController.handleCreateAccount()
  │
  ├─[1] "Is this an existing customer? (Y/N)"
  │
  ├─[PATH A — existing customer]
  │     ├─ InputReader reads: Customer ID
  │     ├─ customerService.getAllCustomers().stream().filter(id match).findFirst()
  │     │     └─ not found → printError(), return early
  │     └─ customer resolved ─────────────────────────────────────────┐
  │                                                                    │
  ├─[PATH B — new customer]                                           │
  │     ├─ InputReader collects: name, age, contact, address, type    │
  │     ├─ CustomerService.registerRegularCustomer()                  │
  │     │     ├─ validateInput()       ← checks no empty strings      │
  │     │     ├─ new RegularCustomer() ← auto-assigns CUST00N id      │
  │     │     └─ Bank.addCustomer()    ← stored in HashMap            │
  │     └─ customer resolved ─────────────────────────────────────────┤
  │                                                                    │
  └─[3] AccountService.createSavingsAccount()  (or createCheckingAccount())
          ├─ Bank.findCustomerById()            ← retrieve Customer object
          ├─ Validate minimum deposit           ← $500 Savings / $0 Checking
          ├─ new SavingsAccount(customer, balance)
          │     └─ Account constructor auto-assigns ACC00N id
          ├─ AccountManager.addAccount()        ← stored in Account[]
          └─ Customer.addAccount()              ← account linked to its owner
```

**Key design points:**
- Both paths converge at the same `AccountService` call — no duplication of account creation logic.
- An existing customer can now accumulate multiple accounts over time, matching the reality that `Customer.accounts` is an `ArrayList` (not a fixed single slot).
- `AccountService` remains the only class that writes to both `AccountManager` and `Customer.addAccount()`, keeping the two registries in sync.

---

### 4.3 Financial Transaction Flow (Deposit or Withdrawal)

**User selects menu option 3 — Process Transaction.**

```
BankController.handleProcessTransaction()
  │
  ├─[1] InputReader reads: account number, transaction type (1=Deposit/2=Withdrawal), amount
  │
  ├─[2] AccountService.getAccountDetails(accountNumber)
  │       └─ AccountManager.findAccountOrThrow()   ← throws AccountNotFoundException if missing
  │             (used to display a confirmation summary to the user)
  │
  ├─[3] User confirms the transaction
  │
  └─[4] AccountService.processTransaction(accountNumber, amount, txnType)
                │
                ├─ Guard: type must be "DEPOSIT" or "WITHDRAWAL"  (IllegalArgumentException)
                │
                ├─ AccountManager.findAccountOrThrow()       ← retrieve live Account object
                │
                ├─ Account.deposit(amount)                   ← or Account.withdraw(amount)
                │     ├─ Guard: account must be Active       (IllegalStateException)
                │     ├─ Guard: amount must be > 0           (InvalidAmountException)
                │     ├─ [withdraw only] validateWithdrawal()
                │     │     ├─ SavingsAccount: balance − amount ≥ $500   (InsufficientFundsException)
                │     │     └─ CheckingAccount: balance − amount ≥ −$1000 (InsufficientFundsException)
                │     ├─ balance updated in place on the Account object
                │     └─ returns new Transaction(accountNumber, type, amount, newBalance)
                │
                └─ TransactionManager.addTransaction(transaction)   ← appended to Transaction[]
```

**Key design point:** `BankController` makes a single call to `AccountService.processTransaction()` for all financial operations — it never calls `deposit()` or `withdraw()` directly. `AccountService.processTransaction()` owns the routing (DEPOSIT vs WITHDRAWAL), finding the account, executing the operation, and logging the result. The `Account` object is still the only place where balance arithmetic happens; `AccountService` coordinates without doing the math itself.

---

### 4.4 View Transaction History Flow

**User selects menu option 4 — View Transaction History.**

```
BankController.handleViewTransactionHistory()
  │
  ├─[1] InputReader reads: account number
  │
  └─[2] AccountService.getTransactionHistory(accountNumber)
          ├─ AccountManager.findAccountOrThrow()       ← verify account exists
          └─ TransactionManager.viewTransactionsByAccount(accountNumber)
                ├─ Iterates Transaction[] in reverse (newest first)
                ├─ Filters by accountNumber
                ├─ Calls Transaction.displayTransactionDetails() for each match
                └─ Prints summary: total deposits, total withdrawals, net change
```

---

### 4.5 Close Account Flow (Soft Delete)

**User selects menu option 5 — Close Account.**

```
BankController.handleCloseAccount()
  │
  ├─[1] InputReader reads: account number
  │
  ├─[2] AccountService.getAccountDetails(accountNumber)
  │       └─ AccountManager.findAccountOrThrow()   ← throws AccountNotFoundException if missing
  │
  ├─[3] BankController checks account.getBalance() != 0
  │       └─ if balance is not zero → prints error, returns early (no service call made)
  │
  ├─[4] User confirms closure
  │
  └─[5] AccountService.closeAccount(accountNumber)
          ├─ AccountManager.findAccountOrThrow()
          ├─ Guard: balance must be exactly 0        (IllegalStateException if not)
          └─ Account.closeAccount()                  ← sets status field to "Closed"
                (account remains in AccountManager array — soft delete, not physical removal)
```

**Key design point:** Closure is a soft delete — the `Account` object stays in the `AccountManager` array permanently with `status = "Closed"`. This preserves the full transaction history in `TransactionManager` and the account reference in the customer's account list. No data is lost. Deposits and withdrawals on a closed account throw `IllegalStateException` at the guard-clause level in `Account.deposit()` / `Account.withdraw()`.

---

### 4.6 Apply Monthly Fees & Interest Flow

**User selects menu option 6 — Apply Monthly Fees & Interest.**

```
BankController.handleApplyFeesAndInterest()
  │
  ├─[1] Shows preview: "$10 fee for non-waived Checking / 3.5% interest for active Savings"
  ├─[2] User confirms
  │
  ├─[3] AccountService.applyMonthlyFees()
  │       ├─ AccountManager.getAccounts()            ← returns Account[] slice (only used slots)
  │       └─ for each account in slice:
  │             if instanceof CheckingAccount:
  │               Transaction t = ca.applyMonthlyFee()
  │                 ├─ if feesWaived → returns null  (skipped)
  │                 └─ if not waived → Account.withdraw(10.00) → returns Transaction
  │               if t != null → TransactionManager.addTransaction(t)
  │       returns count of accounts charged
  │
  └─[4] AccountService.applyInterest()
          ├─ AccountManager.getAccounts()            ← returns Account[] slice
          └─ for each account in slice:
                if instanceof SavingsAccount AND status == "Active":
                  interest = sa.calculateInterest()  ← balance * 0.035
                  Transaction t = account.deposit(interest)
                  TransactionManager.addTransaction(t)
          returns count of accounts credited
```

**Key design point:** Both batch operations go through `AccountManager.getAccounts()` (which returns `Arrays.copyOf(accounts, accountCount)` — only the live portion of the array) and funnel every resulting `Transaction` through `TransactionManager`, keeping the ledger as SSOT. Fee deductions and interest credits are indistinguishable from regular withdrawals/deposits in the ledger — they appear as `WITHDRAWAL` and `DEPOSIT` entries with the same timestamp format.

---

### 4.7 Full System Interaction Map (Mermaid)

```mermaid
sequenceDiagram
    actor User
    participant BC as BankController
    participant IR as InputReader
    participant CS as CustomerService
    participant AS as AccountService
    participant B  as Bank
    participant AM as AccountManager
    participant TM as TransactionManager
    participant C  as Customer
    participant Acc as Account

    %% --- Account Creation ---
    User->>BC: Select "Create Account"
    BC->>IR: "Is this an existing customer? (Y/N)"
    IR-->>BC: choice

    alt Existing customer
        BC->>IR: Collect Customer ID
        IR-->>BC: customerId
        BC->>CS: getAllCustomers() → stream filter by ID
        CS-->>BC: customer (or null → error + return)
    else New customer
        BC->>IR: Collect name, age, contact, address, customer type
        IR-->>BC: raw input
        BC->>CS: registerRegularCustomer(...)
        CS->>CS: validateInput()
        CS->>C: new RegularCustomer()
        CS->>B: addCustomer(customer)
        B-->>CS: stored
        CS-->>BC: customer
    end

    BC->>IR: Collect account type, initial balance
    IR-->>BC: raw input
    BC->>AS: createSavingsAccount(customerId, balance)
    AS->>B: findCustomerById(customerId)
    B-->>AS: customer
    AS->>Acc: new SavingsAccount(customer, balance)
    AS->>AM: addAccount(account)
    AS->>C: addAccount(account)
    AS-->>BC: account

    %% --- Financial Transaction ---
    User->>BC: Select "Process Transaction"
    BC->>IR: Read accountNumber, type, amount
    IR-->>BC: input
    BC->>AS: processTransaction(accountNumber, amount, type)
    AS->>AM: findAccountOrThrow(accountNumber)
    AM-->>AS: account
    AS->>Acc: deposit(amount) or withdraw(amount)
    Acc->>Acc: validate & update balance
    Acc-->>AS: Transaction object
    AS->>TM: addTransaction(transaction)
    TM-->>AS: stored
    AS-->>BC: done

    %% --- View History ---
    User->>BC: Select "View Transaction History"
    BC->>IR: Read accountNumber
    BC->>AS: getTransactionHistory(accountNumber)
    AS->>AM: findAccountOrThrow(accountNumber)
    AS->>TM: viewTransactionsByAccount(accountNumber)
    TM-->>BC: formatted output
```

---

## 5. Data Ownership & Why TransactionManager is the SSOT

### The Problem
A bank system has two representations of financial state:
1. The **live balance** on an `Account` object (used for calculations and rule enforcement).
2. The **historical record** of every event that produced that balance.

Without discipline, these can drift apart — a bug might update the balance without logging the transaction, or vice versa.

### The Solution: TransactionManager as SSOT

Every path through which money moves in this system ends with a call to `TransactionManager.addTransaction()`. This is enforced structurally — `Account.deposit()` and `Account.withdraw()` both *return* a `Transaction` object, and `AccountService` is required to pass that object to `TransactionManager`. There is no way to change a balance through the service layer without also creating a ledger entry.

```
Account.deposit()  ─► returns Transaction  ─► AccountService ─► TransactionManager.addTransaction()
Account.withdraw() ─► returns Transaction  ─► AccountService ─► TransactionManager.addTransaction()
```

As a result:

- `TransactionManager.calculateTotalDeposits(accountNumber)` minus `calculateTotalWithdrawals(accountNumber)` will always equal the current `Account.getBalance()` (assuming the same initial deposit).
- Any discrepancy would point to a bug in the service layer — a very small surface area to audit.
- The ledger is write-once (array append only); no transaction is ever modified or deleted.

### How AccountService Keeps the System Synchronized

`AccountService` is the only class allowed to write to both `AccountManager` and `Customer.addAccount()` simultaneously during account creation. It is also the only class that calls both `Account.deposit()`/`Account.withdraw()` (mutating live state) and `TransactionManager.addTransaction()` (recording history). This central role means:

- **No orphaned accounts:** A new account is always registered in both the manager's lookup array and the owning customer's personal account list in the same method call.
- **No unlogged transactions:** Every balance change produces a `Transaction` that is immediately handed to `TransactionManager`.
- **No partial updates:** If account creation or a transaction fails mid-way (e.g. `AccountNotFoundException`, `InsufficientFundsException`), an exception propagates before any state is written, leaving the system unchanged.

---

## 6. Package Structure

```
com.bank_management_system
 ├── Main.java                    Entry point & dependency wiring
 ├── BankController.java          Menu loop & user interaction
 ├── InputReader.java             Validated console input
 ├── DataInitializer.java         Sample data bootstrap
 │
 ├── bank/
 │    └── Bank.java               Customer registry (HashMap)
 │
 ├── accounts/
 │    ├── Account.java            Abstract domain object
 │    ├── SavingsAccount.java     Savings-specific rules
 │    ├── CheckingAccount.java    Checking-specific rules
 │    ├── AccountManager.java     Account repository (array)
 │    └── AccountService.java     Account & transaction orchestrator
 │
 ├── customers/
 │    ├── Customer.java           Abstract domain object
 │    ├── RegularCustomer.java    Standard customer
 │    ├── PremiumCustomer.java    Fee-exempt customer
 │    └── CustomerService.java    Customer registration orchestrator
 │
 ├── transactions/
 │    ├── Transaction.java        Immutable event record
 │    └── TransactionManager.java Transaction ledger (SSOT)
 │
 └── shared/
      ├── Transactable.java            Interface for Account
      ├── InputValidator.java          Static validation helpers
      ├── InvalidAmountException.java
      ├── InsufficientFundsException.java
      ├── AccountNotFoundException.java
      ├── IllegalStateException.java
      └── IllegalArgumentException.java
```

---

## 7. Data Structures & Algorithms (DSA)

The system does not use a dedicated DSA library, but it applies several classic data structure and algorithm concepts — some explicit in the code, others embedded in the design patterns. This section maps each concept to the exact class and method that implements it.

---

### 7.1 Data Structures

#### Fixed-Size Array (Bounded Buffer)

**Where:** `AccountManager.accounts` (capacity `MAX_ACCOUNTS` = 50), `TransactionManager.transactions` (capacity `MAX_TRANSACTIONS` = 200).

```
Account[]     accounts     = new Account[MAX_ACCOUNTS];          // AccountManager
Transaction[] transactions = new Transaction[MAX_TRANSACTIONS];  // TransactionManager
```

**DSA concept:** A **static/bounded array** paired with a manual integer index (`accountCount`, `transactionCount`) that tracks the next free slot. This is the same technique used to implement a **bounded buffer** or a **circular buffer** (without the wrap-around here). The index is incremented on every `addAccount()` / `addTransaction()` call; a capacity check before insertion prevents array overflow.

**Trade-off vs ArrayList:** O(1) indexed access and zero resizing overhead, but the capacity is fixed at compile time. If more than 50 accounts are needed, update `MAX_ACCOUNTS` and recompile.

---

#### Dynamic Array (ArrayList)

**Where:** `Customer.accounts` — `List<Account> accounts = new ArrayList<>()`.

**DSA concept:** A **dynamic array** that grows automatically as accounts are added to a customer. The JVM doubles the backing array when capacity is exceeded (amortised O(1) append). Used here because the number of accounts per customer is unknown at construction time.

**Trade-off vs fixed array:** Flexible size but has occasional O(n) resizing cost and slightly higher memory overhead.

---

#### Hash Map (Hash Table)

**Where:** `Bank.customers` — `Map<String, Customer> customers = new HashMap<>()`.

```
customers.put(customer.getCustomerId(), customer);   // insert
customers.get(customerId);                           // O(1) lookup
```

**DSA concept:** A **hash table** using customer IDs (strings like `"CUST001"`) as keys. The JVM computes `String.hashCode()`, maps it to a bucket, and stores the entry there. Average-case lookup, insert, and delete are all **O(1)**. Contrast this with `AccountManager`, which stores accounts in a plain array and must do a **linear scan** to find one — O(n). The `Bank` chose a HashMap because customers are looked up by ID from many places; `AccountManager` chose an array because 50 accounts is small enough that O(n) is acceptable.

---

#### Unmodifiable / Defensive-Copy View

**Where:** `Customer.getAccounts()` — returns `Collections.unmodifiableList(accounts)`.

**DSA concept:** A **read-only wrapper** around the internal list. It is not a separate data structure — it is the same backing `ArrayList` exposed through an interface that throws `UnsupportedOperationException` on any mutating call. This is the **Defensive Copy** pattern: callers can iterate and read, but they cannot add or remove accounts without going through `Customer.addAccount()`. Preserves invariants without copying data.

---

#### Append-Only Log (Write-Once Array)

**Where:** `TransactionManager.transactions[]`.

**DSA concept:** An **append-only log** — records are only ever added, never modified or deleted. This mirrors the concept behind **WAL (Write-Ahead Logging)** in databases and **event sourcing** in distributed systems. The current balance of any account is the sum of all its log entries; the array is the authoritative record from which any derived state can be recomputed.

---

#### Auto-Increment ID Counter (Sequence Generator)

**Where:** `Account.accountCounter` (static), `Customer.customerCounter` (static), `Transaction.transactionCounter` (static).

```java
// Example from Account:
private static int accountCounter = 0;
// In constructor:
this.accountNumber = String.format("ACC%03d", ++accountCounter);
```

**DSA concept:** A **monotonically increasing sequence generator** — the same concept as `AUTO_INCREMENT` in SQL or `SEQUENCE` objects in PostgreSQL. The static field is shared across all instances of the class, so every new object gets a globally unique, ordered ID. The format (`%03d`) zero-pads the number to three digits, making IDs lexicographically sortable as well as numerically sortable.

---

### 7.2 Algorithms

#### Linear Search

**Where:** `AccountManager.findAccountOrThrow(String accountNumber)`.

```
for (int i = 0; i < accountCount; i++) {
    if (accounts[i].getAccountNumber().equals(accountNumber)) {
        return accounts[i];   // early exit on match
    }
}
throw new AccountNotFoundException("Account not found: " + accountNumber);
```

**DSA concept:** A **sequential / linear search** — O(n) time, O(1) space. The algorithm scans from index 0 until a match is found or the used portion of the array is exhausted. With a maximum of 50 accounts this is perfectly efficient; a binary search or hash map would add complexity with no meaningful gain at this scale.

**Early-exit / fail-fast:** The `return` inside the loop is a hard exit — the moment a match is found, the method is done. If the loop completes without returning, the `throw` fires. There is no `else` branch needed; `return` and `throw` are both terminators, so code after them in the same path can never run. This is a **guard clause** pattern: establish success early, throw at the bottom as the failure sentinel.

---

#### Reverse Traversal (Newest-First Display)

**Where:** `TransactionManager.viewTransactionsByAccount()`.

```
for (int i = transactionCount - 1; i >= 0; i--) {
    if (transactions[i].getAccountNumber().equals(accountNumber)) {
        transactions[i].displayTransactionDetails();
    }
}
```

**DSA concept:** **Reverse iteration** over an array — traverses from the last used index down to 0. Because transactions are appended in chronological order, iterating in reverse yields the most recent record first. This gives the user a **newest-first / LIFO** view without any sorting or auxiliary data structure. Time complexity remains O(n); no extra memory is used.

**Conceptual link:** This is equivalent to reading a **stack** from top to bottom — even though a stack is not used, the reverse iteration replicates its pop-order traversal.

---

#### Filter + Accumulate (Map-Reduce style)

**Where:** `TransactionManager.calculateTotalDeposits()` and `calculateTotalWithdrawals()` — both delegate to the private `sumByTransactionType(accountNumber, type)` helper.

```
// Pseudocode of sumByTransactionType:
total = 0
for each transaction in transactions[0..transactionCount]:
    if transaction.accountNumber == target AND transaction.type == targetType:
        total += transaction.amount
return total
```

**DSA concept:** A **filter-then-accumulate** pass — a specific form of the general **reduce / fold** operation. The filter predicate is `accountNumber matches AND type matches`; the accumulator is a running sum. In functional programming this is `stream.filter(...).mapToDouble(...).sum()`. The explicit loop here does the same thing imperatively. Time complexity is O(n) per call; the two calls (deposits + withdrawals) make two full passes. Extracting `sumByTransactionType` removes the duplicate loop that previously existed in both methods (DRY principle).

**Optimisation note:** Both totals could be computed in a single pass with two accumulators. The current two-pass approach is clearer to read and adequate given the `MAX_TRANSACTIONS` = 200 ceiling.

---

#### Guard Clause / Early-Exit Validation Chain

**Where:** `Account.deposit()`, `Account.withdraw()`, and all `validateWithdrawal()` implementations.

```
if (status.equals("Closed"))  throw IllegalStateException
if (amount <= 0)              throw InvalidAmountException
validateWithdrawal(amount)    // subclass-specific check
// Only here does balance change
balance += amount;
```

**DSA concept:** **Guard clauses** — a sequence of pre-condition checks that each exit the method immediately on failure. This is an application of **fail-fast** design: the method does the cheapest checks first (status, amount sign) before the domain-specific check (`validateWithdrawal`). Only after all guards pass does the mutation occur. This eliminates deeply nested `if-else` trees and ensures the object is never left in a partially-modified state.

---

#### Polymorphic Dispatch (Runtime Algorithm Selection)

**Where:** `Account.validateWithdrawal()` — abstract in `Account`, implemented differently in `SavingsAccount` and `CheckingAccount`.

| Call site | Runtime type | Algorithm selected |
|---|---|---|
| `account.validateWithdrawal(amount)` | `SavingsAccount` | Enforce `balance - amount >= 500` |
| `account.validateWithdrawal(amount)` | `CheckingAccount` | Enforce `balance - amount >= -1000` |

**DSA concept:** **Dynamic dispatch** — the JVM resolves the method at runtime based on the actual object type, not the declared reference type. This is the object-oriented embodiment of the **Strategy pattern**: the withdrawal-validation algorithm is swapped in transparently depending on account type. `AccountService` calls `account.withdraw()` without knowing or caring which subclass it holds; the correct rule is applied automatically.

---

#### Hash-Based vs Array-Based Lookup: A Direct Comparison

The system contains two registries that serve similar purposes but use different underlying structures. This is a good illustration of choosing the right data structure for the access pattern:

| | `Bank` (customers) | `AccountManager` (accounts) |
|---|---|---|
| **Structure** | `HashMap<String, Customer>` | `Account[]` fixed array |
| **Lookup** | O(1) average by key | O(n) linear scan by account number |
| **Insert** | O(1) average | O(1) append (index known) |
| **Why this choice** | Customers are looked up by ID from multiple service methods; O(1) is worth the hash overhead | Max 50 accounts; O(n) over 50 elements is ~50 comparisons — negligible; simpler to reason about |
| **Capacity** | Unbounded (grows with load factor) | Fixed at 50 |

---

### 7.3 Implicit Algorithmic Concepts Summary

| Concept | Classic DSA Term | Where in the codebase |
|---|---|---|
| Fixed array + manual index | Bounded buffer | `AccountManager`, `TransactionManager` |
| Dynamic array | ArrayList / amortised array | `Customer.accounts` |
| Key-value lookup | Hash table / dictionary | `Bank.customers` (HashMap) |
| Read-only list wrapper | Defensive copy / immutable view | `Customer.getAccounts()` |
| Append-only records | Write-ahead log / event log | `TransactionManager.transactions` |
| Static auto-increment | Sequence generator | `Account`, `Customer`, `Transaction` constructors |
| Sequential scan | Linear search O(n) | `AccountManager.findAccountOrThrow()` |
| Bottom-up iteration | Reverse traversal / stack-order | `TransactionManager.viewTransactionsByAccount()` |
| Filter + sum | Reduce / fold | `TransactionManager.sumByTransactionType()` |
| Pre-condition chain | Guard clauses / fail-fast | `Account.deposit()`, `Account.withdraw()` |
| Abstract method override | Dynamic dispatch / Strategy | `validateWithdrawal()`, `getAccountType()` |

---

## 8. Code Quality Standards

This project follows the **Google Java Style Guide** throughout.

### 8.1 Constant Naming (UPPER_SNAKE_CASE)

All `static final` constants use UPPER_SNAKE_CASE as required by the style guide:

| Constant | Class | Value |
|---|---|---|
| `MAX_ACCOUNTS` | `AccountManager` | 50 |
| `MAX_TRANSACTIONS` | `TransactionManager` | 200 |
| `INTEREST_RATE` | `SavingsAccount` | 0.035 |
| `MINIMUM_BALANCE` | `SavingsAccount` | 500.0 |
| `OVERDRAFT_LIMIT` | `CheckingAccount` | 1000.0 |
| `MONTHLY_FEE` | `CheckingAccount` | 10.0 |

### 8.2 Javadoc Coverage

Every public method across all classes carries a Javadoc comment describing its purpose, parameters (`@param`), and return value (`@return`) or exceptions thrown (`@throws`) where applicable. This applies to abstract methods, interface implementations, getters, and static utility methods.

### 8.3 Method Length (≤ 25 lines)

No public or private method exceeds 25 lines of executable code. Long handlers in `BankController` were decomposed into focused private helpers. `TransactionManager.viewTransactionsByAccount()` was split into four private methods. This keeps each method readable and testable in isolation.

### 8.4 Input Validation

All user-supplied strings are validated by `InputValidator` before they reach the service layer. Each rule is enforced with a regular expression and throws `IllegalArgumentException` on failure, which `BankController` catches and displays without crashing the menu loop.

| Field | Validator method | Rule |
|---|---|---|
| Customer / account name | `validateName` | Letters, spaces, hyphens, apostrophes only |
| Age | `validateAge` | 18–120 inclusive |
| Contact number | `validateContact` | 7–15 characters: digits, spaces, `+`, `-`, `()` |
| Address | `validateAddress` | Letters, digits, spaces, commas, periods, hyphens |
| Account number | `validateAccountNumber` | Must match `ACC` + one or more digits |
| Customer ID | `validateCustomerId` | Must match `CUST` + one or more digits |
| Transaction amount | `validateAmount` | Must be greater than zero |
| Menu choice | `validateMenuChoice` | Must be within the displayed range |

---

## 9. Testing Strategy

### 9.1 Test Structure

Tests live under `src/test/java/com/bank_management_system/`, mirroring the main source tree. Maven picks them up automatically via the Surefire plugin (3.1.2) configured in `pom.xml`.

```
src/test/java/com/bank_management_system/
├── TestResultLogger.java      JUnit 5 TestWatcher — prints PASSED/FAILED per test
├── AccountTest.java           Unit tests for deposit() and withdraw()
├── TransactionManagerTest.java Unit tests for transaction recording and totals
└── AccountServiceTest.java    Unit tests for AccountService using Mockito mocks
```

Dependencies declared in `pom.xml`:

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>5.5.0</version>
    <scope>test</scope>
</dependency>
```

Run all tests with:

```bash
mvn test
```

---

### 9.2 Why Two Different Testing Approaches

The suite uses plain JUnit for some classes and Mockito for others. The choice depends on whether a class has external dependencies:

| Class under test | Approach | Reason |
|---|---|---|
| `Account` (via `SavingsAccount` / `CheckingAccount`) | JUnit only — real objects | `Account` has no dependencies injected through its constructor; it is entirely self-contained. Creating a real instance and calling `deposit()` / `withdraw()` directly is the simplest and most accurate test. |
| `TransactionManager` | JUnit only — real objects | Also self-contained — it holds its own array. A real instance starts empty and behaves predictably. |
| `AccountService` | JUnit + Mockito mocks | `AccountService` depends on `Bank`, `AccountManager`, and `TransactionManager` passed through its constructor. Testing it with real instances would mean all three must work correctly for the test to pass — a failure in `AccountManager` would break an `AccountService` test, making it hard to pinpoint the cause. Mocks isolate `AccountService` completely. |

---

### 9.3 JUnit 5 Patterns Used

| Pattern | Where | Purpose |
|---|---|---|
| `@BeforeEach` | All three test classes | Resets fresh objects before every test so no state leaks between tests |
| `@Test` | Every test method | Marks the method as a test case for JUnit to discover and run |
| `assertEquals(expected, actual, delta)` | `AccountTest`, `TransactionManagerTest` | Verifies balance values and totals with a floating-point tolerance |
| `assertThrows(ExceptionType, lambda)` | `AccountTest`, `AccountServiceTest` | Verifies that an exception of the correct type is thrown — without this, a swallowed exception would silently pass |
| `assertNotNull(value)` | `AccountTest` | Verifies that `deposit()` / `withdraw()` return a Transaction object, not null |
| `assertTrue` / `assertFalse` | `AccountServiceTest` | Verifies boolean state (e.g. `isFeesWaived()`) |
| `@ExtendWith(TestResultLogger.class)` | All three test classes | Attaches the custom `TestWatcher` that prints the formatted PASSED/FAILED console line after each test |

---

### 9.4 Mockito Patterns Used

All Mockito usage is in `AccountServiceTest`. `@ExtendWith(MockitoExtension.class)` activates mock processing for that class.

| Pattern | Annotation / method | What it does |
|---|---|---|
| Create a mock | `@Mock` | Replaces a real `Bank`, `AccountManager`, or `TransactionManager` with a fake that does nothing by default and records every call made to it |
| Inject mocks | `@InjectMocks` | Mockito constructs `AccountService` and automatically passes the three `@Mock` fields into its constructor — no manual wiring needed |
| Stub a return value | `when(mock.method(arg)).thenReturn(value)` | Tells the mock what to return when a specific method is called, so the test controls the code path taken by `AccountService` |
| Stub an exception | `when(mock.method(arg)).thenThrow(exception)` | Forces the error path — used to test how `AccountService` handles `AccountNotFoundException` without needing a broken data store |
| Verify a call happened | `verify(mock, times(n)).method(arg)` | Asserts that `AccountService` called the dependency the expected number of times — proves coordination logic, not just output |
| Verify a call never happened | `verify(mock, never()).method(any())` | Asserts a guard clause stopped execution before a dependency was touched — used to confirm the ledger is never written when a transaction is invalid |

---

### 9.5 Test Coverage Summary

**`AccountTest` — 14 tests**

> Total tests across all files: **72** — run with `mvn test`.

| Test method | What it verifies |
|---|---|
| `depositUpdatesBalance` | Balance increases by the deposited amount |
| `depositReturnsTransactionRecord` | Returns a non-null `Transaction` with correct type, amount, and account number |
| `depositZeroThrowsInvalidAmountException` | Zero deposit is rejected |
| `depositNegativeThrowsInvalidAmountException` | Negative deposit is rejected |
| `depositToClosedAccountThrowsIllegalStateException` | Deposit into a closed account is blocked |
| `withdrawUpdatesBalance` | Balance decreases by the withdrawn amount |
| `withdrawReturnsTransactionRecord` | Returns a non-null `Transaction` with correct type, amount, and account number |
| `withdrawExactMinimumBalanceAllowed` | Withdrawing down to exactly the minimum balance succeeds |
| `withdrawBelowMinimumThrowsException` | Withdrawal that would breach the $500 minimum is rejected |
| `withdrawZeroThrowsInvalidAmountException` | Zero withdrawal is rejected |
| `withdrawFromClosedAccountThrowsIllegalStateException` | Withdrawal from a closed account is blocked |
| `overdraftWithinLimitAllowed` | Checking account balance may go negative within the $1,000 overdraft limit |
| `overdraftExceedThrowsOverdraftLimitExceededException` | Withdrawal that would exceed the overdraft limit throws `OverdraftLimitExceededException` specifically |
| `overdraftLimitExceededIsSubtypeOfInsufficientFunds` | `OverdraftLimitExceededException` is a subtype of `InsufficientFundsException` — inheritance hierarchy is correct |

**`TransactionManagerTest` — 7 tests**

| Test method | What it verifies |
|---|---|
| `addTransactionRecordsDeposit` | A logged deposit appears in `calculateTotalDeposits()` |
| `addTransactionRecordsWithdrawal` | A logged withdrawal appears in `calculateTotalWithdrawals()` |
| `calculateTotalDepositsAggregatesMultiple` | Multiple deposits are summed correctly |
| `calculateTotalWithdrawalsAggregatesMultiple` | Multiple withdrawals are summed correctly |
| `depositsMinusWithdrawalsEqualsNetBalanceChange` | Ledger totals match the actual balance change on the account (SSOT consistency) |
| `calculateTotalDepositsReturnsZeroForUnknownAccount` | Querying a non-existent account returns 0, not an error |
| `transactionLinkedToCorrectAccount` | The `Transaction` object carries the account number it was created for |

**`InputValidatorTest` — 35 tests**

| Test method | What it verifies |
|---|---|
| `validNamePasses` | A normal full name clears the regex |
| `nameWithApostrophePasses` | Apostrophes are allowed (e.g. O'Brien) |
| `nameWithHyphenPasses` | Hyphens are allowed (e.g. Mary-Jane) |
| `blankNameThrowsException` | Blank/whitespace-only name is rejected |
| `nameWithDigitsThrowsException` | Digits in a name are rejected |
| `nameWithSpecialCharsThrowsException` | Symbols in a name are rejected |
| `validAgePasses` | A normal age within range clears the check |
| `minimumAgePasses` | Age 18 is on the boundary — must pass |
| `maximumAgePasses` | Age 120 is on the boundary — must pass |
| `ageBelowMinimumThrowsException` | Age 17 is below the minimum — rejected |
| `ageAboveMaximumThrowsException` | Age 121 is above the maximum — rejected |
| `validPhoneNumberPasses` | Standard format "555-1234" passes |
| `internationalFormatPasses` | "+1 555-1234" (with leading +) passes |
| `tooShortContactThrowsException` | Fewer than 7 characters rejected |
| `lettersInContactThrowsException` | Letters in a phone number rejected |
| `validAddressPasses` | "123 Main St, Springfield" passes |
| `blankAddressThrowsException` | Blank address rejected |
| `addressWithInvalidCharsThrowsException` | Special symbols in address rejected |
| `validAccountNumberPasses` | "ACC001" passes |
| `lowercaseAccountNumberPasses` | "acc001" passes — check is case-insensitive |
| `accountNumberWrongPrefixThrowsException` | "AC001" (wrong prefix) rejected |
| `accountNumberWithNoDigitsThrowsException` | "ACC" with no digits rejected |
| `blankAccountNumberThrowsException` | Blank account number rejected |
| `validCustomerIdPasses` | "CUST001" passes |
| `lowercaseCustomerIdPasses` | "cust001" passes — check is case-insensitive |
| `invalidCustomerIdPrefixThrowsException` | "CUS001" (wrong prefix) rejected |
| `blankCustomerIdThrowsException` | Blank customer ID rejected |
| `validAmountPasses` | Positive amount passes |
| `zeroAmountThrowsInvalidAmountException` | Zero amount throws `InvalidAmountException` |
| `negativeAmountThrowsInvalidAmountException` | Negative amount throws `InvalidAmountException` |
| `validMenuChoicePasses` | Choice within range passes |
| `minimumMenuChoicePasses` | Choice equal to min is on the boundary — passes |
| `maximumMenuChoicePasses` | Choice equal to max is on the boundary — passes |
| `choiceBelowMinThrowsException` | Choice below min rejected |
| `choiceAboveMaxThrowsException` | Choice above max rejected |

**`AccountServiceTest` — 16 tests (Mockito)**

| Test method | What it verifies |
|---|---|
| `depositLogsTransactionInLedger` | `addTransaction()` is called exactly once on the mocked ledger after a deposit |
| `withdrawalLogsTransactionInLedger` | `addTransaction()` is called exactly once after a withdrawal |
| `invalidTransactionTypeThrowsIllegalArgumentException` | Unknown type throws exception; `addTransaction()` is never called |
| `unknownAccountThrowsAccountNotFoundException` | `AccountNotFoundException` from the mock propagates out of `AccountService` |
| `createSavingsAccountRegistersWithAccountManager` | `addAccount()` is called on the mocked `AccountManager` after successful creation |
| `createSavingsAccountBelowMinimumThrowsException` | Balance below $500 throws `InvalidAmountException`; `addAccount()` is never called |
| `createCheckingAccountWaivesFeeForPremiumCustomer` | Account created for a `PremiumCustomer` has `isFeesWaived() == true` |
| `createCheckingAccountDoesNotWaiveFeeForRegularCustomer` | Account created for a `RegularCustomer` has `isFeesWaived() == false` |
| `closeAccountWithNonZeroBalanceThrowsIllegalStateException` | Closing an account with a remaining balance is blocked |
| `getTransactionHistoryCallsBothManagerAndLedger` | Both `findAccountOrThrow()` and `viewTransactionsByAccount()` are called exactly once |
| `applyMonthlyFeesChargesNonWaivedCheckingAccount` | Fee is deducted and logged; count returns 1 |
| `applyMonthlyFeesSkipsWaivedCheckingAccount` | Premium account is skipped; ledger untouched; count returns 0 |
| `applyMonthlyFeesIgnoresSavingsAccounts` | SavingsAccount is not a CheckingAccount — ignored entirely |
| `applyInterestCreditsSavingsAccount` | Interest deposit is logged; count returns 1 |
| `applyInterestSkipsClosedSavingsAccount` | Closed account is skipped; ledger untouched; count returns 0 |
| `applyInterestIgnoresCheckingAccounts` | CheckingAccount is not a SavingsAccount — ignored entirely |
