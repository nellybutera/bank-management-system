# CLAUDE.md — AI Assistance Log

This file documents the contributions made by Claude (Anthropic) as an AI assistant during the development and documentation of this project.

---

## What Claude Helped With

### 1. System Architecture Documentation (`architecture.md`)

Claude analyzed the entire Java codebase by reading every source file and produced a full architecture document covering:

- **Architecture Overview** — identified and named the four-tier layered pattern (Presentation → Service → Manager → Domain) and the key design principles in use (single responsibility, dependency injection, abstract base classes, SSOT)
- **Class Responsibilities Table** — documented every class across all layers with its role and key behaviour
- **Dependency Graph** — mapped how `Main` wires all dependencies together and how class hierarchies relate
- **Sequence Flows** — wrote step-by-step flows for application startup, account creation, financial transactions, and transaction history viewing
- **Mermaid Diagram** — produced a full `sequenceDiagram` covering all three main user workflows
- **Data Ownership & SSOT section** — explained why `TransactionManager` is the Single Source of Truth, how `Account` is structurally forced to return a `Transaction` object (making unlogged mutations impossible), and how `AccountService` acts as the synchronization point

### 2. DSA Analysis (`architecture.md §7`)

Claude identified and documented every Data Structure and Algorithm concept present in the codebase — both explicit and implicit — including:

- Fixed-size arrays as bounded buffers (`AccountManager`, `TransactionManager`)
- Dynamic array / ArrayList (`Customer.accounts`)
- Hash map / hash table (`Bank.customers`) with O(1) lookup analysis
- Unmodifiable list / defensive-copy view (`Customer.getAccounts()`)
- Append-only log pattern (`TransactionManager`) linked to WAL and event sourcing concepts
- Static auto-increment sequence generator (all three ID counters)
- Linear search with null-vs-exception variant explanation (`AccountManager.findAccount`)
- Reverse traversal / LIFO view (`TransactionManager.viewTransactionsByAccount`)
- Filter + accumulate / reduce-fold pattern (`calculateTotalDeposits/Withdrawals`)
- Guard clause / fail-fast chain (`Account.deposit`, `Account.withdraw`)
- Polymorphic dispatch as runtime algorithm selection (`validateWithdrawal`)
- A direct comparison table of HashMap vs array lookup trade-offs within the same codebase

### 3. Beginner's Guide (`dummyguide.md`)

Claude wrote a plain-English companion document for developers new to coding, covering the same content as `architecture.md` but without assumed knowledge:

- Real-world analogies for every class (filing cabinets, ledger books, bank officers, cookie cutters)
- Step-by-step walkthroughs of each menu option in plain English
- OOP concepts (encapsulation, inheritance, abstraction, polymorphism) explained with daily-life comparisons
- DSA concepts (arrays, ArrayList, HashMap, linear search, reverse traversal, guard clauses) explained without jargon
- A quick-reference table mapping every class to a plain-English role
- A recommended reading order for navigating the source code for the first time

### 4. README Rebalancing (`README.md`)

Claude restructured the README to avoid duplicating content already covered in depth in `architecture.md`:

- Added a documentation index table at the top so readers know which file serves their needs
- Replaced verbose class-breakdown paragraphs with a compact project structure tree
- Replaced verbose OOP and DSA prose sections with scannable tables and pointers to `architecture.md`
- Kept setup instructions, features list, and exceptions table — content that belongs in a README
- Result: README serves as a project landing page; `architecture.md` serves as the technical reference

---

## How Claude Worked

Claude used the following process for this project:

1. **Codebase exploration** — launched a specialised Explore agent to read all Java source files and map class fields, constructors, methods, and inter-class references before writing anything
2. **Bottom-up analysis** — started from domain objects (`Account`, `Customer`, `Transaction`), moved up through managers, then services, then the controller/IO layer
3. **No code was written or modified** — Claude only produced documentation files; the Java source code was not changed
4. **Three-document strategy** — rather than one giant document, content was split by audience: README (everyone), architecture.md (developers), dummyguide.md (beginners)

---

## Files Produced by Claude

| File | Status | Description |
|---|---|---|
| `architecture.md` | Created | Full technical architecture documentation |
| `dummyguide.md` | Created | Beginner-friendly plain-English guide |
| `README.md` | Updated | Rebalanced — removed duplication, added doc index |
| `CLAUDE.md` | Created | This file |

---

## Model Information

- **Model:** Claude Sonnet 4.6 (`claude-sonnet-4-6`)
- **Interface:** Claude Code (CLI)
- **Date of assistance:** 2026-03-13
