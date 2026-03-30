# Beginner's Guide to the Bank Management System

This guide explains the same system documented in `architecture.md` but in plain language — no assumed knowledge required. If you are new to coding, start here.

---

## What is this program?

It is a **pretend bank** that runs in your terminal (the black text window). There is no real money, no real people, and no internet connection. It is a simulation — a program that *acts like* a bank so you can learn how real banking software is structured.

When you run it, you get a menu like this:

```
1. Create Account
2. View All Accounts
3. Process Transaction
4. View Transaction History
5. Close Account
6. Apply Monthly Fees & Interest
7. View Customer Accounts
8. Exit
```

You type a number and the program does that thing. That's it from the user's perspective. But underneath, dozens of Java classes are working together to make each option happen safely and correctly.

---

## What is a "class" and what is an "object"?

Before anything else, you need to understand these two words because the whole program is built from them.

**A class is a blueprint.**
Think of a class like a cookie cutter. The cookie cutter is not a cookie itself — it is the *shape* that tells you how to make a cookie.

**An object is the thing made from the blueprint.**
When you press the cookie cutter into dough, you get an actual cookie. That cookie is the object.

In this program, `SavingsAccount` is a class (the blueprint). When John Smith opens an account, the program runs `new SavingsAccount(johnSmith, 5000)` and creates an actual object — John's specific account with his name and his $5,000.

You can make as many cookies as you want from one cutter. Similarly, the program can make as many `SavingsAccount` objects as it needs, all from the same class.

---

## The people and things in this bank

Think of the program as a real bank with staff, customers, filing cabinets, and a ledger book. Each class plays one of those roles.

### The Customer (`Customer`, `RegularCustomer`, `PremiumCustomer`)

A `Customer` object holds one person's information:
- their name
- their age
- their phone number
- their address
- a list of the bank accounts they own

`Customer` is **abstract** — this is just a fancy word meaning "this blueprint is incomplete on purpose." You cannot create a plain `Customer`; you must create either a `RegularCustomer` or a `PremiumCustomer`.

Think of it like ordering coffee. You cannot order just "coffee" — you have to say "latte" or "espresso". `RegularCustomer` and `PremiumCustomer` are the specific types; `Customer` is just the shared idea of what a customer is.

**What is the difference between Regular and Premium?**
- A `RegularCustomer` pays a $10 monthly fee if they have a checking account.
- A `PremiumCustomer` gets that fee waived automatically. The program checks `customer.isEligibleForFeeWaiver()` — Regular returns `false`, Premium returns `true`.

### The Bank Account (`Account`, `SavingsAccount`, `CheckingAccount`)

An `Account` object holds one account's information:
- the account number (`ACC001`, `ACC002`, …)
- who owns it (a reference to the `Customer` object)
- the current balance
- whether it is open or closed

Again, `Account` is abstract. You must create a `SavingsAccount` or a `CheckingAccount`.

**SavingsAccount rules:**
- You cannot let the balance fall below $500.
- It has a 3.5% interest rate. When you use menu option 6, the program calculates `balance × 3.5%` and adds that amount to your account as a deposit.

**CheckingAccount rules:**
- You can go $1,000 *below* zero (this is called an overdraft — the bank is lending you money temporarily).
- There is a $10 monthly fee unless you are a Premium customer.

### The Filing Cabinet for Accounts (`AccountManager`)

The `AccountManager` is like a physical filing cabinet with 50 slots. It holds all the accounts in the whole bank.

When you create a new account, it gets added to a slot. When you look up an account by number (`ACC003`), the program searches through the cabinet one slot at a time until it finds it. If it reaches the end without finding it, it immediately throws an error rather than returning nothing — this is called **fail-fast**. The search itself is called a **linear search**.

### The Filing Cabinet for Customers (`Bank`)

The `Bank` class is like a second filing cabinet, but this one uses **tabs with labels** instead of numbered slots. Each tab is a customer ID (`CUST001`, `CUST002`, …) and behind the tab is the customer's information.

Looking someone up by their tab label is much faster than searching slot by slot — this is called a **hash map lookup** and it works in effectively zero time regardless of how many customers there are.

### The Ledger Book (`TransactionManager` + `Transaction`)

Every time money moves — deposit or withdrawal — the bank writes it down in a ledger book. That ledger book is the `TransactionManager`, and each line in the book is a `Transaction` object.

`TransactionManager` is broken into several focused helper methods so that no single method is too long to understand at a glance:
- `countMatchingTransactions()` — counts how many entries belong to a given account
- `printTransactionTable()` — prints each matching entry as a formatted row
- `printTransactionSummary()` — prints the deposit/withdrawal/net totals at the bottom
- `sumByTransactionType()` — the shared calculation used by both `calculateTotalDeposits()` and `calculateTotalWithdrawals()` to avoid writing the same loop twice

A `Transaction` records:
- which account was involved
- whether it was a deposit or withdrawal
- how much money moved
- what the balance was *after* the operation
- the exact date and time it happened

**Why does this matter?** Because the ledger is the *permanent truth*. The balance on the `Account` object tells you the current state; the ledger tells you *how* it got there. If there is ever a question like "wait, where did that $200 go?", you look at the ledger.

This makes `TransactionManager` the **Single Source of Truth (SSOT)** — the one place that has the full, unalterable history of everything that happened.

---

## The staff who run the bank

The classes above store data (customers, accounts, transactions). Now here are the classes that actually *do* things.

### The Account Officer (`AccountService`)

`AccountService` is the person who sits at the desk and handles everything to do with accounts. You don't go directly to the filing cabinet yourself — you talk to the officer and they handle it.

When you want to create an account, the officer:
1. Looks up the customer in the `Bank` cabinet.
2. Creates the right type of account (`SavingsAccount` or `CheckingAccount`).
3. Puts it in the `AccountManager` cabinet.
4. Also links it to the customer's own list of accounts.

When you want to process a transaction, the officer handles everything through one method — `processTransaction()`:
1. Checks that the type is either DEPOSIT or WITHDRAWAL (rejects anything else immediately).
2. Finds the account in the `AccountManager`.
3. Tells the account object to add or subtract the money (the account does its own math).
4. Takes the receipt (`Transaction` object) that the account produces.
5. Writes it in the ledger (`TransactionManager`).

This means the receptionist (`BankController`) only needs to make one call — not two separate ones for deposits and withdrawals.

Notice: the officer never does the math themselves. They hand off the deposit to the `Account` object, which knows the rules. The officer's job is coordination, not calculation.

### The Customer Registration Desk (`CustomerService`)

`CustomerService` handles signing up new customers. Before creating anyone, it runs four checks using `InputValidator`:

- **Name** — must contain only letters, spaces, hyphens, or apostrophes. Numbers and symbols are rejected. (So "John Smith" passes but "J0hn$" does not.)
- **Age** — must be between 18 and 120. Anything outside that range is rejected.
- **Contact number** — must be a valid phone format: 7 to 15 characters, digits only plus optional `+`, `-`, spaces, or parentheses. (So "+1 555-1234" passes but "abc" does not.)
- **Address** — must contain only letters, digits, spaces, commas, periods, or hyphens. (So "123 Main St, Springfield" passes.)

If any check fails, `CustomerService` throws an error immediately and no customer is created. If everything looks good, it creates the customer and adds them to the `Bank` cabinet.

### The Receptionist (`BankController`)

`BankController` is the first thing a user interacts with. It shows the menu, reads your choice, and then calls the right officer.

It does not know banking rules. It just reads your input, calls `AccountService` or `CustomerService`, and shows you the result or any error messages.

### The Notepad (`InputReader`)

`InputReader` is the tool the receptionist uses to read what you type. It is smarter than a plain keyboard reader though — if you type letters where a number is expected, it says "invalid, try again" and waits. It keeps asking until you give a valid answer.

---

## How everything starts up (`Main` + `DataInitializer`)

When you run the program, `Main.java` runs first. Think of `Main` as the bank opening for the day:

1. It creates an empty `Bank` (customer cabinet).
2. It creates an empty `AccountManager` (account cabinet, 50 slots).
3. It creates an empty `TransactionManager` (ledger, 200 pages).
4. It creates an `AccountService` and a `CustomerService`, giving them access to those cabinets.
5. It calls `DataInitializer`, which pre-fills the bank with 5 pretend customers and 6 accounts so there is something to see right away (Michael Chen has both a Checking and a Savings account).
6. It creates `BankController` and tells it to start the menu loop.

After step 6, the program sits and waits for you to press a key.

---

## How the menu options work, step by step

### Option 1 — Create Account

```
You type: 1

Receptionist (BankController) asks for: name, age, phone, address, customer type, account type, initial balance

Registration Desk (CustomerService):
  → checks your name/contact/address aren't blank
  → creates a RegularCustomer or PremiumCustomer
  → puts them in the Bank cabinet

Account Officer (AccountService):
  → looks up the customer just created
  → checks you deposited enough (at least $500 for Savings, $0 for Checking)
  → creates the account
  → puts it in the AccountManager cabinet
  → also links it to the customer's personal account list

Result: one new customer and one new account exist in the system.
```

### Option 2 — View All Accounts

```
You type: 2

BankController calls AccountService.displayAllAccounts()
AccountService tells AccountManager to print the table
AccountManager loops through all 50 slots, skipping empty ones, and prints each account
```

### Option 3 — Process Transaction

```
You type: 3

BankController asks: account number? deposit or withdrawal? amount?

AccountService finds the account in AccountManager

You are shown a confirmation: "Deposit $200 into ACC001 — confirm?"

If you say yes:
  BankController calls AccountService.processTransaction(accountNumber, amount, type)
  AccountService routes to deposit or withdrawal, then:
  Account checks:
    - Is the account open?          (if closed → error)
    - Is the amount positive?       (if not → error)
    - Does it pass withdrawal rules? (minimum balance / overdraft)
  Account updates its own balance
  Account creates a Transaction receipt
  AccountService takes the receipt and gives it to TransactionManager
  TransactionManager writes it in the ledger
```

### Option 4 — View Transaction History

```
You type: 4

BankController asks: account number?
AccountService verifies the account exists
TransactionManager searches the ledger (from newest to oldest) for all entries with that account number
Each matching entry is printed with amount, type, and balance after
A summary shows: total deposited, total withdrawn, net change
```

### Option 5 — Close Account

This is called a **soft delete**. "Soft" means we do not erase the account from memory — we just mark it as "Closed" so no more deposits or withdrawals can be made. The history is kept forever.

```
You type: 5

BankController asks: account number?
AccountService finds the account

If the balance is not $0.00:
  → Error: "Withdraw all funds before closing"
  → Nothing happens, account stays open

If the balance IS $0.00:
  → BankController asks: "Are you sure? (Y/N)"
  → If Y: AccountService calls account.closeAccount()
           The account's status changes from "Active" to "Closed"
           The account stays in the system — its history is preserved
           Any future deposit or withdrawal on it will be blocked with an error
```

**Why not just delete it?** Because the ledger (TransactionManager) still has all the transactions linked to that account number. Deleting the account would leave orphaned records. Soft delete keeps everything consistent.

### Option 6 — Apply Monthly Fees & Interest

This is a **batch operation** — one button that runs a job across all accounts at once. Think of it like a bank's end-of-month automated process.

```
You type: 6

BankController shows a preview and asks: "Proceed? (Y/N)"

If Y:
  AccountService.applyMonthlyFees():
    → Gets the full list of accounts from AccountManager
    → For each CheckingAccount:
         If the customer is Regular (fee NOT waived):
           Deducts $10 from the balance
           Writes a WITHDRAWAL transaction to the ledger
         If the customer is Premium (fee IS waived):
           Skips it entirely

  AccountService.applyInterest():
    → Gets the full list of accounts from AccountManager
    → For each SavingsAccount that is still Active:
         Calculates interest: balance × 3.5%
         Adds that amount to the balance as a deposit
         Writes a DEPOSIT transaction to the ledger

BankController prints: "Fees applied: X accounts | Interest credited: Y accounts"
```

**Why does this go through the ledger?** Because every money movement — even automated ones — must be recorded in TransactionManager. That way if someone checks their transaction history, they will see the fee charge or interest credit just like any other transaction.

### Option 7 — View Customer Accounts

```
You type: 7

BankController asks: Customer ID (e.g. CUST001)
Searches the CustomerService for that customer
Calls customer.viewCustomerAccounts()
  → Prints a table of all accounts belonging to that customer
  → Shows total assets across all their accounts
```

---

## What "OOP" means and how it shows up here

OOP stands for **Object-Oriented Programming**. It is just a style of writing code where you organise things as objects (like the bank analogy above) instead of one giant list of instructions. Here are the four main ideas and where you can see them:

### Encapsulation — "Don't touch my stuff directly"

In real life, you don't reach into the bank vault yourself. You ask the teller. Encapsulation is the same idea.

In this program, `Account.balance` is `private` — nothing outside the class can read or change it directly. The only way to change the balance is through `deposit()` or `withdraw()`, which run the safety checks first. This means the balance can *only* change through safe, logged paths.

### Inheritance — "Children get what parents have"

`SavingsAccount` and `CheckingAccount` both need an account number, a customer reference, a balance, and a status. Instead of writing all that twice, they both **inherit** it from `Account`. They only add what makes them different (minimum balance rules, overdraft rules).

Imagine a job posting that says "same responsibilities as a bank teller, plus you also handle foreign currency." The "plus" part is what the subclass adds; everything else is inherited.

### Abstraction — "Here's the contract, you figure out the details"

`Account` says: "every account type *must* have a `validateWithdrawal()` method." It does not say what that method should do. That is left to `SavingsAccount` and `CheckingAccount` to decide. This is abstraction — you define *what* must exist without saying *how* it works.

### Polymorphism — "Same call, different behaviour"

When `AccountService` calls `account.withdraw(200)`, it does not know or care if `account` is a `SavingsAccount` or `CheckingAccount`. Java automatically uses the right version of `validateWithdrawal()` for whatever type `account` actually is at runtime.

This is like saying "close the account" to a teller. They know the procedure for a savings account is different from a checking account, so they follow the right steps automatically.

---

## What "DSA" means and how it shows up here

DSA stands for **Data Structures and Algorithms**. A **data structure** is how you organise information (like a list vs a filing cabinet with tabs). An **algorithm** is the step-by-step procedure you use to do something (like how you search through that cabinet).

### Arrays — the numbered slots

`AccountManager` has 50 numbered slots for accounts. `TransactionManager` has 200 numbered slots for transactions. These are **arrays** — like an egg carton. You know exactly how many spots there are, and each spot has a number. You keep a counter of how many are filled.

**Limitation:** Once full (50 accounts), no more can be added. The number 50 is baked into the code.

### ArrayList — the expandable list

A customer's personal list of their own accounts (`Customer.accounts`) uses an `ArrayList`. This is like a notebook that adds extra pages when it runs out of room. You don't know ahead of time how many accounts one customer will have, so you let it grow.

### HashMap — the tabbed filing cabinet

`Bank.customers` is a `HashMap`. Instead of numbered slots, it uses labels — the customer ID. When you look up `CUST003`, the HashMap goes directly to that label. You don't scan from `CUST001` onwards. This is essentially **O(1) lookup** — it takes the same amount of time whether there are 3 customers or 300,000.

### Linear Search — checking one by one

When you look up `ACC027` in `AccountManager`, the program checks slot 0, slot 1, slot 2 … until it finds it. This is a **linear search**. With only 50 accounts, this is extremely fast. It would be slower with a million accounts, but for this project it's perfectly fine.

### Reverse Traversal — reading the ledger backwards

The ledger (`TransactionManager`) stores transactions oldest-first (because they are appended in order). When you view history, the program reads the array *backwards* — from the last slot down to the first — so you see the newest transaction at the top. No sorting needed; just flip the reading direction.

### Guard Clauses — check first, act second

Before the program touches your balance, it runs a series of checks:
1. Is the account open?
2. Is the amount greater than zero?
3. Would this withdrawal break the minimum balance or overdraft limit?

If any check fails, the program stops immediately and tells you what went wrong. It never half-completes an operation. This is the **guard clause** pattern — check all the rules before doing anything.

---

## Input validation — catching bad data before it causes problems

Before the program does anything with information you type, it runs it through a checklist called `InputValidator`. Think of it like a security guard at the front door who checks your ID before letting you in.

Each rule is enforced the same way: if the input passes the check, the program continues. If it fails, the program throws an error, shows you what went wrong, and brings you back to the menu. Nothing is half-done.

Here is what gets checked and why:

| What you typed | What is checked | Why |
|---|---|---|
| A customer or account name | Letters, spaces, hyphens, apostrophes only — no digits or symbols | A real name like "O'Brien" should work; "H4x0r!" should not |
| Age | Must be between 18 and 120 | Under 18 is too young to open an account; over 120 is not realistic |
| Phone number | 7 to 15 characters; digits plus optional `+`, `-`, spaces, or brackets | Covers international formats like `+44 20 7946 0958` |
| Address | Letters, digits, spaces, commas, periods, hyphens | Allows real addresses like `123 Main St, Springfield` |
| Account number (e.g. to look one up) | Must start with `ACC` followed by digits | Catches typos like `AC001` or `ACC` with no number |
| Customer ID (e.g. to view accounts) | Must start with `CUST` followed by digits | Same idea — catches format mistakes before a lookup is attempted |
| Transaction amount | Must be greater than zero | You cannot deposit or withdraw $0 or a negative amount |
| Menu choice | Must be one of the numbers shown | Typing `9` when the menu only goes to `8` is rejected immediately |

The reason all of this is handled before the service layer sees the data is so that `AccountService` and `CustomerService` can trust what they receive. They do not need to add their own defensive checks on top — the validation already happened.

---

## How all the files talk to each other — the simple picture

```
You (keyboard)
    ↓
InputReader           reads and validates what you type
    ↓
BankController        shows the menu, calls the right service
    ↓
AccountService        the central coordinator for all account work
 ├──→ Bank            look up a customer by ID (O(1) HashMap)
 ├──→ AccountManager  store or find accounts (array + linear search)
 ├──→ Account         do the math, enforce the rules
 │       └── returns a Transaction receipt
 └──→ TransactionManager   write the receipt into the permanent ledger
```

Every path money takes goes through `AccountService`, and every financial event ends up in `TransactionManager`. Nothing is skipped. Nothing is done twice. That is why the system stays consistent.

---

## Quick reference: plain-English class descriptions

| Class | Think of it as… |
|---|---|
| `Main` | The bank opening for the day — sets everything up |
| `DataInitializer` | The person who stocks the shelves before opening |
| `BankController` | The receptionist at the front desk |
| `InputReader` | The notepad the receptionist uses to take your request |
| `CustomerService` | The customer registration desk |
| `AccountService` | The account officer who coordinates everything — creates accounts, handles transactions, closes accounts, runs batch fee/interest jobs |
| `Bank` | The tabbed customer filing cabinet |
| `AccountManager` | The numbered account filing cabinet |
| `TransactionManager` | The permanent ledger book |
| `Customer` | A blueprint for any bank customer |
| `RegularCustomer` | A standard customer — pays fees |
| `PremiumCustomer` | A high-value customer — fees waived |
| `Account` | A blueprint for any bank account |
| `SavingsAccount` | An account with a $500 minimum balance rule |
| `CheckingAccount` | An account with overdraft protection |
| `Transaction` | One line written in the ledger |
| `Transactable` | A promise that says "this object can process transactions" |
| `InputValidator` | A checklist that validates name, age, contact, address, account number, customer ID, amount, and menu choice before any of them are used |

---

## Unit testing — how we know the code works

### What is a unit test?

A **unit test** is a small program that automatically checks one specific thing about your code. Instead of running the whole bank application and pressing buttons manually, you write tests that call individual methods and check whether the result is what you expected.

For example, instead of:
> "Run the app, create an account, go to Process Transaction, type in $200, confirm, then look at the balance and hope it says $1,200"

You write:
```
savings.deposit(200);
check that savings.getBalance() equals 1200
```

That check runs in milliseconds and will tell you immediately if something is wrong.

---

### JUnit — the testing framework

**JUnit** is the tool that runs the tests and reports whether they passed or failed. Think of JUnit as a referee. Your test says "I expect X" and JUnit checks whether the code actually produced X. If it did — PASSED. If it didn't — FAILED, and it tells you exactly what went wrong.

Every test method in this project is marked with `@Test` so JUnit knows to run it. `@BeforeEach` is a setup step that runs before every single test to create fresh objects — like resetting the board before each game.

---

### Mockito — the fake dependency tool

Some classes depend on other classes to do their job. `AccountService`, for example, needs `Bank`, `AccountManager`, and `TransactionManager` to be up and running before it can do anything.

When you test `AccountService`, you don't actually want to test whether `AccountManager` works at the same time — you only want to test whether `AccountService` itself is doing the right thing. If both break at the same time, it's impossible to tell which one caused the failure.

**Mockito** solves this by creating **fakes** (called mocks) that pretend to be those dependencies. You tell the fake what to return when asked, and afterwards you can check whether `AccountService` called it the right way.

Real-world analogy: imagine testing a waiter (AccountService). Instead of using a real kitchen (AccountManager), you use a fake kitchen that gives back whatever you tell it to. That way, if the test fails, you know the waiter made a mistake — not the kitchen.

The three main things Mockito does in this project:

| What | How | Plain-English meaning |
|---|---|---|
| Create a fake | `@Mock` | "Replace the real thing with a controllable fake" |
| Tell the fake what to return | `when(...).thenReturn(...)` | "When someone asks this question, give back this answer" |
| Check the fake was used | `verify(...)` | "Did the waiter actually place the order with the kitchen?" |

---

### The three test files

| File | Tests | What it checks |
|---|---|---|
| `AccountTest.java` | 8 tests | `deposit()` and `withdraw()` directly — balance updates, exception conditions, overdraft rules |
| `TransactionManagerTest.java` | 4 tests | Transaction recording and totals — proves the ledger stores and sums correctly |
| `AccountServiceTest.java` | 5 tests (Mockito) | `AccountService` coordination — uses Mockito to verify it calls the right dependencies the right number of times |

Run all 17 tests with: `mvn test`

---

## Where to go next

- Read `architecture.md` for the full technical version of everything explained here, including Mermaid sequence diagrams and complexity analysis.
- Look at `AccountService.java` first when reading the source code — it is the centre of the whole system and the best starting point.
- Then read `Account.java` to understand how deposits and withdrawals actually work.
- Then read `TransactionManager.java` to see how the ledger is stored and queried.
