# Git Workflow

This document describes the branching strategy, commit conventions, and cherry-pick log used across the Bank Account Management System project.

---

## Branch Structure

```
main                                        # stable, production-ready code
feature/refactor                            # clean code, Javadoc, naming standards
feature/exceptions                          # custom exception handling and validation
feature/testing                             # JUnit 5 test suite
feature/collections                         # Java Collections API migration
feature/file-persistence                    # file I/O with NIO streams
feature/regex-validation                    # regex-based input validation
feature/concurrency                         # thread safety and concurrent simulation
feature/clean-code-and-missing-lab2-functionality  # ExceptionTest, docs, README update
```

---

## Commit Conventions

All commits follow Conventional Commits style with past-tense imperative verbs:

```
added <thing>
updated <thing> to <behaviour>
extracted <thing> into <location>
replaced <old> with <new>
fixed <bug>
```

No `feat:` / `fix:` prefixes. No co-authorship tags.

---

## Common Commands

```bash
# Create and switch to a new feature branch
git checkout -b feature/exceptions

# Stage specific files for a focused commit
git add src/main/java/com/bank_management_system/exceptions/InvalidAmountException.java

# Commit with a descriptive message
git commit -m "added InvalidAmountException for zero and negative deposit amounts"

# View commit history on the current branch
git log --oneline

# Cherry-pick a specific commit from another branch
git cherry-pick <commit-hash>

# Stash before cherry-picking to avoid conflicts
git stash
git cherry-pick <commit-hash>
git stash pop

# Merge a feature branch into main
git checkout main
git merge feature/exceptions
```

---

## Why Cherry-Pick?

Merging an entire branch brings in all of its commits — even ones unrelated to the current branch's purpose. Cherry-pick selectively applies only the commit you need, keeping each branch focused.

**Example:** `feature/testing` needs the refactored `TransactionManager` from `feature/refactor` to compile its tests, but not the Javadoc or naming-convention commits. Cherry-picking the single refactoring commit keeps the test branch clean.

---

## Cherry-Pick Log

| Commit | From branch | To branch | What it brought | Why |
|---|---|---|---|---|
| `58e273b` | `feature/exceptions` | `feature/refactor` | Try-catch blocks in BankController, CustomerService; InputValidator with regex validators | Brought input validation into the refactor branch without merging unrelated exception history |
| `c392b8d` | `feature/refactor` | `feature/exceptions` | `MAX_ACCOUNTS` / `MAX_TRANSACTIONS` constants, TransactionManager split into private helpers, `sumByTransactionType` DRY method | Tests need the refactored TransactionManager methods on this branch |
| `4776cf3` | `feature/refactor` | `feature/exceptions` | Javadoc on Account, AccountService, Bank, Customer, CheckingAccount, SavingsAccount, InputReader, DataInitializer | Completes Javadoc coverage across all classes used in the test suite |
| `731b732` | `feature/refactor` | `feature/exceptions` | Javadoc on PremiumCustomer and RegularCustomer | Finishes the Javadoc pass for the full customer class hierarchy |
| `9c0ad63` | `feature/refactor` | `feature/exceptions` | BankController refactored into helper methods (≤25 lines each), `validateAccountNumber` / `validateCustomerId` calls added | Brings the full validation flow onto this branch before integration testing |
| `77a9103` | `feature/refactor` | `feature/exceptions` | README, architecture.md, dummyguide.md updated with refactoring and validation changes | Keeps documentation consistent across both branches |

---

## Minimum Commit Count per Phase

The assignment requires at least 3 commits per feature branch to demonstrate progressive development. Each branch in this project has 4–8 commits covering: initial implementation, tests, Javadoc, and integration fixes.
