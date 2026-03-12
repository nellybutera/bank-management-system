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

When you create a new account, it gets added to a slot. When you look up an account by number (`ACC003`), the program searches through the cabinet one slot at a time until it finds it. This is called a **linear search**.

### The Filing Cabinet for Customers (`Bank`)

The `Bank` class is like a second filing cabinet, but this one uses **tabs with labels** instead of numbered slots. Each tab is a customer ID (`CUST001`, `CUST002`, …) and behind the tab is the customer's information.

Looking someone up by their tab label is much faster than searching slot by slot — this is called a **hash map lookup** and it works in effectively zero time regardless of how many customers there are.

### The Ledger Book (`TransactionManager` + `Transaction`)

Every time money moves — deposit or withdrawal — the bank writes it down in a ledger book. That ledger book is the `TransactionManager`, and each line in the book is a `Transaction` object.

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

When you want to deposit money, the officer:
1. Finds the account in the `AccountManager`.
2. Tells the account object to add the money (the account does its own math).
3. Takes the receipt (`Transaction` object) that the account produces.
4. Writes it in the ledger (`TransactionManager`).

Notice: the officer never does the math themselves. They hand off the deposit to the `Account` object, which knows the rules. The officer's job is coordination, not calculation.

### The Customer Registration Desk (`CustomerService`)

`CustomerService` handles signing up new customers. Before creating anyone, it checks:
- Is the name blank? (Reject.)
- Is the contact number blank? (Reject.)
- Is the address blank? (Reject.)

If everything looks good, it creates the customer and adds them to the `Bank` cabinet.

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
5. It calls `DataInitializer`, which pre-fills the bank with 5 pretend customers and 5 accounts so there is something to see right away.
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
  AccountService tells the Account to deposit/withdraw
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
| `InputValidator` | A checklist that validates inputs before they are used |

---

## Where to go next

- Read `architecture.md` for the full technical version of everything explained here, including Mermaid sequence diagrams and complexity analysis.
- Look at `AccountService.java` first when reading the source code — it is the centre of the whole system and the best starting point.
- Then read `Account.java` to understand how deposits and withdrawals actually work.
- Then read `TransactionManager.java` to see how the ledger is stored and queried.
