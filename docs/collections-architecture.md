# Collections Architecture

This document explains every collection and functional programming choice in the Bank Account Management System — what was chosen, why, and what trade-offs were considered.

---

## Data Structure Decisions

### `AccountManager` — `LinkedHashMap<String, Account>`

```java
private final LinkedHashMap<String, Account> accounts = new LinkedHashMap<>();
```

**Why LinkedHashMap over plain HashMap?**
- O(1) average lookup by account number (`ACC001`) — replaces the original O(n) linear scan through an array
- Maintains insertion order so `viewAllAccounts()` always displays accounts in the order they were created, giving consistent output across runs
- `findAccountOrThrow()` becomes a single `map.get()` call instead of a loop

**Why not TreeMap?**
- TreeMap sorts keys alphabetically (ACC001, ACC002, …) which gives the same display order as insertion in this case, but at O(log n) cost per operation. LinkedHashMap gives O(1) with the same display behaviour — no reason to pay the log factor.

**Why not ArrayList?**
- ArrayList lookup by account number requires iterating every element — O(n). With a map, lookup is O(1) regardless of how many accounts exist.

---

### `TransactionManager` — `Collections.synchronizedList(new ArrayList<>())`

```java
private final List<Transaction> transactions = Collections.synchronizedList(new ArrayList<>());
```

**Why ArrayList?**
- Transactions are append-only — no deletions, no lookup by key. ArrayList gives O(1) amortised append and O(1) index access, which is all that's needed.
- Unlike accounts, transactions are never looked up directly by ID from the service layer. They are always filtered by account number using a stream, so a map key would add overhead without benefit.

**Why synchronizedList?**
- Multiple threads (from `ConcurrencyUtils`) may call `addTransaction()` concurrently. `synchronizedList` wraps every individual list method in a `synchronized` block, preventing corruption from concurrent `add()` calls.
- `addTransaction()` itself is also marked `synchronized` to guard the method as a whole — not just the list write. This prevents a thread from entering the method while another is mid-execution.

**Why not ConcurrentLinkedQueue or CopyOnWriteArrayList?**
- `CopyOnWriteArrayList` makes a full copy of the backing array on every write — expensive for a ledger that can have hundreds of entries.
- `ConcurrentLinkedQueue` doesn't support index access or the `stream()` + `sorted()` pattern cleanly.
- `synchronizedList(ArrayList)` is the standard choice for a list that's written occasionally and read frequently via streams.

---

### `Bank` — `HashMap<String, Customer>`

```java
private final Map<String, Customer> customers = new HashMap<>();
```

**Why HashMap?**
- Customer lookup by ID (`CUST001`) is O(1). This is a pure lookup map — insertion order doesn't matter, and no sorting is needed.

---

### `Customer.accounts` — `ArrayList<Account>`

```java
private final List<Account> accounts = new ArrayList<>();
```

**Why ArrayList?**
- A customer typically holds 1–3 accounts. The list is small, append-only, and iterated for display. ArrayList is the simplest correct choice.

---

### `FilePersistenceService.customerCache` — `HashMap<String, Customer>`

```java
private final Map<String, Customer> customerCache = new HashMap<>();
```

**Why a cache map?**
- Michael Chen has two accounts (Checking and Savings). Without the cache, parsing two account lines for the same customer would create two separate `Customer` objects with the same ID — breaking object identity and duplicating the customer in the system.
- `computeIfAbsent(customerId, id -> buildCustomer(parts))` creates the Customer once on first encounter and returns the same instance on every subsequent line with the same ID.

---

## Functional Programming Patterns

### Stream Pipelines

Streams are used for all collection processing — no explicit `for` loops in the service layer.

| Operation | Where | Stream operators |
|---|---|---|
| Load accounts from file | `FilePersistenceService.loadAccounts()` | `lines()` → `filter()` → `map()` → `collect()` |
| Load transactions from file | `FilePersistenceService.loadTransactions()` | `lines()` → `filter()` → `map(Transaction::fromLine)` → `collect()` |
| Filter active Checking accounts | `AccountService.applyMonthlyFees()` | `stream()` → `filter()` → `map()` → `filter()` → `collect()` |
| Sort transactions newest-first | `TransactionManager`, `StatementGenerator` | `stream()` → `sorted(Comparator.reversed())` → `collect()` |
| Group by transaction type | `FunctionalUtils.groupByType()` | `stream()` → `collect(groupingBy())` |
| Sum amounts | `FunctionalUtils.sumAmounts()` | `stream()` → `map()` → `reduce(0.0, Double::sum)` |
| Find largest transaction | `FunctionalUtils.largestTransaction()` | `stream()` → `max(Comparator.comparingDouble())` |
| Sum balances | `FunctionalUtils.totalBalance()` | `stream()` → `mapToDouble()` → `sum()` |
| Find customer by ID | `BankController` | `stream()` → `filter()` → `findFirst()` → `ifPresentOrElse()` |
| Apply interest to all savings | `AccountService.applyInterest()` | `stream()` → `forEach()` |
| Batch deposit (parallel) | `ConcurrencyUtils` | `parallelStream()` → `filter()` → `forEach()` |

### `reduce()` for Accumulation

`FunctionalUtils.sumAmounts()` demonstrates explicit reduction:

```java
return transactions.stream()
        .map(Transaction::getAmount)       // Stream<Double>
        .reduce(0.0, Double::sum);         // identity 0.0, accumulator Double::sum
```

The identity element `0.0` means an empty list returns zero (not `Optional.empty()`), which is correct for financial totals. `Double::sum` is a method reference to `Double.sum(a, b)` — the binary accumulator.

### Method References

Method references replace single-method lambdas throughout:

```java
.map(Transaction::fromLine)             // static: calls Transaction.fromLine(line)
.map(this::parseAccount)                // instance: calls this.parseAccount(line)
fees.forEach(transactionManager::addTransaction)   // instance on captured object
.forEach(Transaction::displayTransactionDetails)   // instance on stream element
accounts.forEach(accountManager::addAccount)
```

### `Predicate<String>` as First-Class Values

`ValidationUtils` compiles each regex once and exposes it as a `Predicate<String>`:

```java
public static final Predicate<String> isValidEmail =
        email -> test(EMAIL, email);
```

This allows validation rules to be passed as arguments, composed with `.and()` / `.or()`, or tested inline:

```java
if (ValidationUtils.isValidEmail.test(email)) { ... }
```

### Functional Interfaces: `Comparator` and `Predicate`

`Comparator` is used as a first-class parameter — the sort strategy is decided at the call site and injected into `viewTransactionsByAccount()`:

```java
Comparator<Transaction> sortOrder = sortBy.equalsIgnoreCase("AMOUNT")
        ? Comparator.comparingDouble(Transaction::getAmount).reversed()
        : Comparator.comparing(Transaction::getCreatedAt).reversed();

transactionManager.viewTransactionsByAccount(accountNumber, sortOrder);
```

`Predicate<Account>` is built inline and passed to `findAccountsMatching()`:

```java
Predicate<Account> activeChecking = account ->
        account instanceof CheckingAccount && account.getStatus().equalsIgnoreCase("Active");

accountManager.findAccountsMatching(activeChecking)
```

---

## Thread Safety Design

Two complementary layers prevent race conditions during concurrent transactions:

```
Layer 1 — Account.updateBalance() is synchronized
    Only one thread can modify a single account's balance at a time.

Layer 2 — TransactionManager.addTransaction() is synchronized
    Only one thread can append to the ledger at a time.
    The backing ArrayList is also wrapped in synchronizedList()
    for safety on individual list method calls.
```

`parallelStream()` in `ConcurrencyUtils` uses the common fork-join pool. Because `updateBalance()` is `synchronized`, deposits across different accounts run truly in parallel while deposits to the same account are serialised correctly.

---

## Migration Summary (Arrays → Collections)

| Class | Before | After | Benefit |
|---|---|---|---|
| `AccountManager` | `Account[50]` fixed array | `LinkedHashMap<String, Account>` | O(1) lookup, no capacity limit |
| `TransactionManager` | `Transaction[200]` fixed array | `synchronizedList(ArrayList)` | Thread-safe, unbounded, stream-compatible |
