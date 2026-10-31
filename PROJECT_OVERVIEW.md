# Coding Battle Platform — Comprehensive Project Overview

> **Course**: Object-Oriented Programming using Java (CSE203) — Java Mini Project  
> **Mandated Architecture**: JavaFX 21 (Frontend) + Core Java 17+ (Backend/OOP/DSA) + JDBC + MySQL  
> **Target Environments**: JavaFX 21 Native Desktop Application + Node.js/Express Web Platform  
> **Curriculum Alignment**: Progressive Core Java to Advanced Java Programming Course  
> **Document Purpose**: Complete Architectural Specification, OOP & DSA Foundations, and Technical Overview  

---

## 📑 Table of Contents
1. [Executive Summary & Problem Statement](#1-executive-summary--problem-statement)
2. [Dual Architecture & Tech Stack](#2-dual-architecture--tech-stack)
3. [Object-Oriented Programming (OOP) Design](#3-object-oriented-programming-oop-design)
4. [Custom Data Structures & Algorithms (DSA) Layer](#4-custom-data-structures--algorithms-dsa-layer)
5. [Sandboxed Java Code Execution Engine](#5-sandboxed-java-code-execution-engine)
6. [Resilient Dual-Mode Database Architecture](#6-resilient-dual-mode-database-architecture)
7. [Progressive Core Java Course Curriculum (28 Ordered Programs)](#7-progressive-core-java-course-curriculum-28-ordered-programs)
8. [Viva Voce Questions & Academic Evaluation Guide](#8-viva-voce-questions--academic-evaluation-guide)

---

## 1. Executive Summary & Problem Statement

### Academic Context
Computer Science education requires continuous hands-on programming with instant, deterministic verification. Traditional computer lab evaluation relies on manual code grading, offline assignments, or external platforms like LeetCode or HackerRank. However, generic competitive programming puzzles often focus on advanced algorithmic trivia rather than reinforcing the structured learning milestones of an institutional **Core Java Course**.

### Platform Solution
The **Coding Battle Platform** is a complete, self-contained educational competition system offering:
- **Curriculum-Aligned Learning**: 28 structured Java lab programs sequenced chronologically from foundational syntax and control flow to object-oriented encapsulation and recursion.
- **Dual User Personas**:
  - **Student Portal**: Practice problems, participate in live timed battles with synchronized countdown timers, submit code, view test case diagnostics, climb global and contest leaderboards, and unlock milestone achievement badges.
  - **Admin / Faculty Portal**: Comprehensive CRUD controls over problem descriptions, test case datasets, live/upcoming battles, student directories, and system-wide submission audit logs.
- **Process-Isolated Execution Sandbox**: Evaluates student code via dynamic compilation (`javax.tools.JavaCompiler` and `javac`) in an isolated subprocess with hard CPU timeouts and security filters.
- **Zero-Config Database Resilience**: Automatic failover between production MySQL and embedded H2, ensuring zero presentation interruptions during evaluation.

---

## 2. Dual Architecture & Tech Stack

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           CLIENT / PRESENTATION LAYER                           │
├────────────────────────────────────────┬────────────────────────────────────────┤
│         Desktop Client (JavaFX 21)     │         Web Client (SPA)               │
│ - FXML Declarative Layouts             │ - Semantic HTML5 + Custom Modern CSS   │
│ - CSS Dark Theme Styling               │ - Pure ES6+ Client Engine (No Bundler) │
│ - Timeline Live Countdown Clocks       │ - Web Audio API Interactive Sound FX   │
│ - SplitPane Reactive Code Workspace    │ - Mobile & Tablet Responsive Viewport  │
└───────────────────┬────────────────────┴───────────────────┬────────────────────┘
                    │                                        │
┌───────────────────▼────────────────────────────────────────▼────────────────────┐
│                             APPLICATION & LOGIC LAYER                           │
├────────────────────────────────────────┬────────────────────────────────────────┤
│          Java Core Service Layer       │          Node.js / Express Server      │
│ - UserService, ProblemService          │ - RESTful API Endpoints (/api/*)       │
│ - BattleService, SubmissionService     │ - In-Memory State Synchronizer         │
│ - LeaderboardService, ScoreCalculator  │ - Process-Spawn Java Execution Pipeline│
│ - Custom DSA Algorithms & Data Buffers │ - Role-Based JWT/Session Authorizer    │
└───────────────────┬────────────────────┴───────────────────┬────────────────────┘
                    │                                        │
┌───────────────────▼────────────────────────────────────────▼────────────────────┐
│                         EXECUTION & DATA PERSISTENCE LAYER                      │
├────────────────────────────────────────┬────────────────────────────────────────┤
│      Isolated Java Sandbox Engine      │         Dual-Engine Database           │
│ - javax.tools.JavaCompiler + javac     │ - Primary: MySQL 8.x Server            │
│ - Security AST / Regex Whitelist Scan  │ - Failover: Embedded H2 Database       │
│ - Watchdog Timeout Killer Subprocess   │ - Auto-detection via DatabaseConnection│
│ - Normalized Diff Test Validator       │ - Parameterized JDBC PreparedStatements│
└────────────────────────────────────────┴────────────────────────────────────────┘
```

### Technology Matrix
- **Programming Language**: Java 17 / 21 LTS (Modern Core Java, Concurrency, Process API).
- **Desktop Framework**: JavaFX 21 with FXML layouts and modular CSS.
- **Web Stack**: Node.js, Express.js 4.x, Semantic HTML5, Custom Responsive Vanilla CSS, ES6+ JavaScript.
- **Persistence Engines**: JDBC, MySQL 8.x, H2 Database Engine 2.2.x.
- **Build & Management**: Apache Maven 3.9.x, Windows Batch Launcher (`run.bat`), PowerShell Launcher (`run.ps1`).

---

## 3. Object-Oriented Programming (OOP) Design

The system is designed around the four foundational pillars of Object-Oriented Programming:

### A. Abstraction
- **Base Domain Class** (`com.codingbattle.model.User`):
  - Defines common user contracts (`userId`, `username`, `email`, `password`, `role`, `createdAt`).
  - Declares the abstract method `public abstract String getRoleDescription()`, forcing subclasses to provide concrete role representations.
- **Engine Interfaces**:
  - `CodeEvaluator`: Defines `Submission evaluate(Submission submission, Problem problem)` to decouple evaluation logic from the service layer.
  - `ScoreCalculator`: Defines `int calculateScore(...)` utilizing the Strategy Pattern for flexible difficulty and speed bonus policies.

### B. Inheritance
- `Student` extends `User`:
  - Inherits authentication and profile fields while adding student-specific properties: `rollNumber`, `department`, `semester`, `codingStreakDays`, and unlocked achievements list.
- `Admin` extends `User`:
  - Inherits shared credentials while adding faculty-specific properties: `department`, `designation`, `accessLevel`, and administrative privileges.

### C. Polymorphism
- **Dynamic Method Dispatch**: Calling `user.getRoleDescription()` produces distinct outputs depending on whether the object is a `Student` or an `Admin`.
- **Strategy-Based Scoring**: `DefaultScoreCalculator` applies difficulty multipliers (`EASY`: $1.0\times$, `MEDIUM`: $1.5\times$, `HARD`: $2.0\times$) and time bonuses dynamically.

### D. Encapsulation & Composition
- **Encapsulation**: All fields in model classes are declared `private` with validated getters and setters to protect internal state.
- **Composition**:
  - A `Battle` has a collection of `Problem` instances.
  - A `Problem` has a collection of `TestCase` entities (sample and hidden).
  - A `Submission` has a collection of `TestCaseResult` diagnostics.

---

## 4. Custom Data Structures & Algorithms (DSA) Layer

Rather than relying entirely on generic collection libraries, core platform operations use custom-built DSA implementations:

| Custom Data Structure | Class Name | Asymptotic Time Complexity | Auxiliary Space | Operational Platform Function |
| :--- | :--- | :--- | :--- | :--- |
| **Binary Max-Heap** | `LeaderboardHeap` | Build: $\mathcal{O}(N)$<br>Extract Top-K: $\mathcal{O}(K \log N)$ | $\mathcal{O}(N)$ | Dynamically extracts the Top-K student competitors for dashboard ribbons without full $\mathcal{O}(N \log N)$ sorting. |
| **Custom QuickSort** | `QuickSortRanking` | Average: $\mathcal{O}(N \log N)$<br>Worst: $\mathcal{O}(N \log N)$ | $\mathcal{O}(\log N)$ | Sorts the complete student ranking by points descending with tie-breakers on problems solved using median-of-three pivot selection. |
| **Generic LIFO Stack** | `EditorUndoStack<T>` | Push: $\mathcal{O}(1)$<br>Pop: $\mathcal{O}(1)$ | $\mathcal{O}(S)$ | Powers the code editor undo and redo action buffers with dynamic array capacity management. |
| **Thread-Safe FIFO Queue** | `SubmissionQueue` | Enqueue: $\mathcal{O}(1)$<br>Dequeue: $\mathcal{O}(1)$ | $\mathcal{O}(Q)$ | Buffers incoming student code submissions for asynchronous background worker evaluation. |
| **Search & Inverted Index** | `ProblemSearchIndex` | ID Lookup: $\mathcal{O}(1)$<br>Points Search: $\mathcal{O}(\log N)$ | $\mathcal{O}(N)$ | Provides $\mathcal{O}(1)$ hash map lookup combined with $\mathcal{O}(\log N)$ Binary Search across points-sorted arrays. |

---

## 5. Sandboxed Java Code Execution Engine

The platform compiles and executes student submissions inside a safe execution sandbox:

```
[Student Source Code]
        │
        ▼
[Security AST & Token Scanner] ──(Forbidden tokens found)──► Reject [SECURITY_VIOLATION]
        │ (Passed)
        ▼
[Create Isolated Temp Directory]
        │
        ▼
[javax.tools.JavaCompiler / javac] ──(Syntax error)───────► Return [COMPILATION_ERROR]
        │ (Compiled OK)
        ▼
[ProcessBuilder Subprocess Spawn]
        │
        ├─────────────────────────────────────────────────┐
        ▼                                                 ▼
[Watchdog Timer Thread]                      [Test Case Stream Feeder]
(Limit: 2000ms)                              (Feeds standard input)
        │                                                 │
        ▼ (Timeout triggered)                             ▼
[process.destroyForcibly()]                  [Capture stdout & stderr]
Status: TIME_LIMIT_EXCEEDED                               │
                                                          ▼
                                             [Normalized Diff Engine]
                                             Status: ACCEPTED / WRONG_ANSWER
```

1. **Security Scanner**: Inspects code before compilation to reject hazardous operations: `Runtime.getRuntime()`, `ProcessBuilder`, `System.exit()`, and reflection APIs.
2. **Subprocess Isolation**: Runs student code in an independent JVM process (`java -cp <dir> Solution`) with restricted heap memory (`-Xmx256m`).
3. **Watchdog Timeout Guardian**: Enforces a strict 2000ms CPU timeout via `process.waitFor(timeout, TimeUnit.MILLISECONDS)`. Infinite loops (`while(true)`) are terminated forcibly.
4. **Normalized Diff Output Comparator**: Strips carriage returns (`\r\n` vs `\n`) and trailing whitespace so operating system newline differences do not cause false evaluation failures.

---

## 6. Resilient Dual-Mode Database Architecture

The persistence layer guarantees **uninterrupted demonstration reliability**:

```
                          [Application Launch]
                                   │
                                   ▼
                    [Attempt MySQL Connection Pool]
                    (jdbc:mysql://localhost:3306)
                                   │
                     ┌─────────────┴─────────────┐
                     │                           │
              (MySQL Online)              (MySQL Offline)
                     │                           │
                     ▼                           ▼
            [Activate MySQL Mode]      [Activate Embedded H2 Mode]
                     │                 (jdbc:h2:./data/coding_battle_db)
                     │                           │
                     └─────────────┬─────────────┘
                                   │
                                   ▼
                    [Execute DatabaseInitializer]
                     - Verify Table DDL
                     - Seed 28 Java Course Programs
                     - Seed Demo Accounts & Battles
```

- **Automatic Failover**: When launched without a running MySQL instance, `DatabaseConnection.java` detects the timeout and activates embedded H2 in MySQL compatibility mode (`MODE=MySQL`).
- **Data Integrity**: Uses standard ANSI SQL and parameterized `PreparedStatement` queries across both engines to prevent SQL injection vulnerabilities.

---

## 7. Progressive Core Java Course Curriculum (28 Ordered Programs)

The platform is seeded with 28 structured Java programs arranged by difficulty and pedagogical sequence:

### Module 1: Basics, Syntax, Variables & Operators
1. **Java 01: Welcome to Java & Console Output** (`STRINGS` | `EASY` | 100 pts)  
   *Concept*: `System.out.println()`, `Scanner` input, string concatenation.
2. **Java 02: Arithmetic Operators Calculator** (`MATHEMATICS` | `EASY` | 100 pts)  
   *Concept*: Arithmetic operators (`+`, `-`, `*`, `/`, `%`), operator precedence.
3. **Java 03: Temperature Converter (Celsius to Fahrenheit)** (`MATHEMATICS` | `EASY` | 100 pts)  
   *Concept*: Floating-point data types (`double`), formula conversion.
4. **Java 04: Swap Two Variables (Without Third Variable)** (`MATHEMATICS` | `EASY` | 100 pts)  
   *Concept*: Arithmetic/bitwise in-place variable swapping.

### Module 2: Decision Making & Conditionals (if-else, switch)
5. **Java 05: Check Even or Odd Number** (`MATHEMATICS` | `EASY` | 100 pts)  
   *Concept*: Branching logic, modulo operator `% 2 == 0`.
6. **Java 06: Maximum of Three Numbers** (`MATHEMATICS` | `EASY` | 100 pts)  
   *Concept*: Relational operators (`>`), logical operators (`&&`), nested `if-else`.
7. **Java 07: Leap Year Checker** (`MATHEMATICS` | `EASY` | 100 pts)  
   *Concept*: Multi-condition boolean logic: `(year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)`.
8. **Java 08: Vowel or Consonant (Switch Case)** (`STRINGS` | `EASY` | 100 pts)  
   *Concept*: `switch(char)` branching, character case normalization (`toLowerCase`).

### Module 3: Loops & Iterations (for, while, do-while)
9. **Java 09: Factorial of a Number** (`MATHEMATICS` | `EASY` | 100 pts)  
   *Concept*: Accumulator pattern, iterative loops, overflow prevention (`long`).
10. **Java 10: Fibonacci Sequence Generator** (`MATHEMATICS` | `EASY` | 100 pts)  
    *Concept*: State update across loop iterations ($f_0, f_1 \to f_2$).
11. **Java 11: Prime Number Verification** (`MATHEMATICS` | `EASY` | 100 pts)  
    *Concept*: Loop optimization with $\mathcal{O}(\sqrt{N})$ boundary check.
12. **Java 12: Integer Palindrome & Reversal** (`MATHEMATICS` | `EASY` | 100 pts)  
    *Concept*: While loop extracting digits via `% 10` and `/= 10`.
13. **Java 13: Armstrong Number Checker** (`MATHEMATICS` | `EASY` | 100 pts)  
    *Concept*: Multi-pass number inspection: counting digits and accumulating power sums.
14. **Java 14: Sum of Digits** (`MATHEMATICS` | `EASY` | 100 pts)  
    *Concept*: Loop-based digit extraction and running sum.

### Module 4: 1D and 2D Arrays in Java
15. **Java 15: Array Min, Max and Sum** (`ARRAYS` | `EASY` | 100 pts)  
    *Concept*: 1D array traversal, boundary initialization, running sum.
16. **Java 16: Reverse Array In-Place** (`ARRAYS` | `EASY` | 100 pts)  
    *Concept*: Two-pointer technique swapping elements symmetrically.
17. **Java 17: Linear Search in Array** (`SEARCHING` | `EASY` | 100 pts)  
    *Concept*: Sequential array traversal returning 0-based index or `-1`.
18. **Java 18: Bubble Sort Implementation** (`SORTING` | `EASY` | 100 pts)  
    *Concept*: Nested loop sorting, adjacent element comparison and swapping.
19. **Java 19: 2D Matrix Addition** (`ARRAYS` | `MEDIUM` | 200 pts)  
    *Concept*: Two-dimensional arrays (`int[][]`), nested row and column iteration.
20. **Java 20: 2D Matrix Transpose** (`ARRAYS` | `MEDIUM` | 200 pts)  
    *Concept*: Matrix index transposition ($M[j][i]$) across non-square dimensions.

### Module 5: Strings & String Methods in Java
21. **Java 21: String Palindrome Checker** (`STRINGS` | `EASY` | 100 pts)  
    *Concept*: String methods `charAt()`, `equalsIgnoreCase()`, two-pointer check.
22. **Java 22: Count Vowels, Consonants and Digits** (`STRINGS` | `EASY` | 100 pts)  
    *Concept*: Character classification (`Character.isLetter()`, `Character.isDigit()`).
23. **Java 23: Reverse Words in a Sentence** (`STRINGS` | `MEDIUM` | 200 pts)  
    *Concept*: String splitting on regex (`split("\\s+")`), reverse traversal, `StringBuilder`.
24. **Java 24: Character Frequency Counter** (`STRINGS` | `MEDIUM` | 200 pts)  
    *Concept*: Frequency array mapping (`int[26]`) and formatted output generation.

### Module 6: Methods, Recursion & OOP Concepts
25. **Java 25: Euclidean GCD via Recursion** (`RECURSION` | `EASY` | 100 pts)  
    *Concept*: Base case ($b = 0$) and recursive reduction ($\text{gcd}(b, a \% b)$).
26. **Java 26: Fast Power Function $X^N$ via Recursion** (`RECURSION` | `MEDIUM` | 200 pts)  
    *Concept*: Divide-and-conquer recursion ($x^{n/2} \times x^{n/2}$) in $\mathcal{O}(\log N)$ time.
27. **Java 27: Student Marks & Grade Calculator (OOP Class)** (`MATHEMATICS` | `MEDIUM` | 200 pts)  
    *Concept*: Class definition with private fields, aggregate percentage calculation, letter grades.
28. **Java 28: Bank Account Transaction Simulator (Encapsulation)** (`MATHEMATICS` | `HARD` | 300 pts)  
    *Concept*: Object encapsulation, data validation on deposit/withdraw, state persistence.

---

## 8. Viva Voce Questions & Academic Evaluation Guide

### Q1: How does your application demonstrate Object-Oriented Programming (OOP)?
- **Abstraction**: `User` is an abstract base class enforcing common user contracts and `abstract String getRoleDescription()`; `CodeEvaluator` and `ScoreCalculator` are interfaces decoupling business services from evaluation engines.
- **Inheritance**: `Student` and `Admin` extend `User`, inheriting shared authentication fields while adding role-specific attributes.
- **Polymorphism**: `getRoleDescription()` is overridden across `Student` and `Admin`; `DefaultScoreCalculator` applies polymorphic scoring formulas based on difficulty and speed.
- **Encapsulation**: All domain models feature private variables accessible only through validated getters and setters.
- **Composition**: `Battle` contains a collection of `Problem` objects; `Problem` contains a collection of `TestCase` objects.

### Q2: Why did you implement custom data structures instead of standard Java collections?
Standard library collections are battle-tested, but implementing custom classes demonstrates mastery of internal data structure mechanics and algorithmic complexity:
- `LeaderboardHeap` extracts the Top-K competitors in $\mathcal{O}(K \log N)$ time, outperforming an $\mathcal{O}(N \log N)$ full sort when $K \ll N$.
- `QuickSortRanking` provides median-of-three partitioning and tie-breaking logic tailored to competitive programming scoring.
- `EditorUndoStack` and `SubmissionQueue` demonstrate LIFO and thread-safe FIFO data structures in real-world application contexts.

### Q3: How do you safely evaluate student Java code without crashing the host application?
- **Process Isolation**: The code is compiled into a temporary workspace and launched as an independent subprocess using `ProcessBuilder`.
- **Timeout Watchdog**: `Process.waitFor(timeLimit, TimeUnit.MILLISECONDS)` monitors execution. If the solution exceeds the timeout (e.g., an infinite `while(true)` loop), `process.destroyForcibly()` immediately terminates the process and marks the submission as `TIME_LIMIT_EXCEEDED`.
- **Security Scanner**: A token scanner inspects the code before compilation for forbidden operations (`Runtime.getRuntime`, `ProcessBuilder`, `System.exit`, reflection).
