# 🏛️ Coding Battle Platform — System Architecture & Technical Blueprint

> **Course**: Object-Oriented Programming using Java (CSE203) — Java Mini Project  
> **Mandated Architecture**: JavaFX (Frontend) + Core Java (Backend & OOP/DSA) + JDBC (Database Connectivity) + MySQL (Storage)  
> **Primary Client**: JavaFX 21 Native Desktop Client (Hardware Accelerated FXML & CSS)  
> **Language**: Java 17+ (Core & Advanced Java)  
> **Database**: Dual-Engine (Production MySQL with zero-config Embedded H2 auto-failover)  

---

## 📑 Table of Contents
1. [Executive Architectural Overview](#1-executive-architectural-overview)
2. [Multi-Tier System Architecture](#2-multi-tier-system-architecture)
3. [Component Hierarchy & Data Flow](#3-component-hierarchy--data-flow)
4. [Sandboxed Java Code Execution Engine](#4-sandboxed-java-code-execution-engine)
5. [Real-Time Notification & Task Broadcast Engine](#5-real-time-notification--task-broadcast-engine)
6. [Resilient Dual-Mode Database Architecture](#6-resilient-dual-mode-database-architecture)
7. [Design Patterns & Structural Blueprint](#7-design-patterns--structural-blueprint)
8. [Thread Safety, Concurrency & Process Watchdogs](#8-thread-safety-concurrency--process-watchdogs)

---

## 1. Executive Architectural Overview

The **Coding Battle Platform** is engineered as a resilient, high-performance competitive programming ecosystem designed for higher-education computer science coursework, lab practical examinations, and timed coding competitions.

The platform provides a dual-client interface:
- **Native Desktop Client (JavaFX 21)**: Provides a high-fidelity desktop IDE experience with hardware acceleration, split-pane coding, and low-latency local execution.
- **Modern Web Client (HTML5 / ES6 / Express)**: Accessible across any modern browser, tablet, or workstation without requiring local compilation tools installed on student machines.

```mermaid
flowchart TD
    subgraph Clients["Presentation & Client Layer"]
        A1["JavaFX 21 Desktop GUI<br/>(FXML + CSS + Reactive Controllers)"]
        A2["Web Single Page Application<br/>(Semantic HTML5 + Modern CSS + ES6)"]
    end

    subgraph Gateways["Access & Communication Layer"]
        B1["Java Desktop Controllers<br/>(SessionManager + NavigationCallback)"]
        B2["Express.js REST Gateway / JavaHttpServer<br/>(CORS + Rate Limiting + JSON APIs)"]
    end

    subgraph LogicLayer["Business & Algorithmic Core"]
        C1["Service Layer<br/>(UserService, ProblemService, BattleService, AssignmentService)"]
        C2["Custom DSA Core<br/>(LeaderboardHeap, QuickSortRanking, SubmissionQueue, EditorUndoStack)"]
    end

    subgraph ExecutionTier["Sandboxed Execution Sandbox"]
        D1["Source Security Scanner<br/>(Regex AST Token Filter)"]
        D2["Dynamic Compiler<br/>(javax.tools.JavaCompiler / javac)"]
        D3["Subprocess Watchdog<br/>(ProcessBuilder + Timeout Kill Switch)"]
    end

    subgraph DataTier["Data Persistence Tier"]
        E1["DAO Abstraction Layer<br/>(UserDAO, ProblemDAO, SubmissionDAO, ScoreDAO, BattleDAO)"]
        E2[("Production MySQL 8.x<br/>(InnoDB, ACID Compliant)")]
        E3[("Embedded H2 Database<br/>(In-Memory / File Auto-Failover)")]
    end

    A1 --> B1
    A2 --> B2
    B1 --> C1
    B2 --> C1
    C1 --> C2
    C1 --> D1
    D1 --> D2 --> D3
    C1 --> E1
    E1 -.->|Primary Route| E2
    E1 -.->|Auto Fallback| E3

    classDef primary fill:#2563eb,stroke:#1d4ed8,color:#fff,font-weight:bold;
    classDef secondary fill:#059669,stroke:#047857,color:#fff,font-weight:bold;
    classDef warning fill:#d97706,stroke:#b45309,color:#fff,font-weight:bold;
    classDef neutral fill:#334155,stroke:#1e293b,color:#fff;

    class A1,A2 primary;
    class B1,B2 neutral;
    class C1,C2 secondary;
    class D1,D2,D3 warning;
    class E1,E2,E3 neutral;
```

---

## 2. Multi-Tier System Architecture

The architecture is partitioned into five distinct, strictly decoupled tiers:

| Tier | Primary Responsibilities | Key Technologies |
| :--- | :--- | :--- |
| **1. Client Tier** | User interface rendering, syntax highlighting, Monaco-like editor behavior, undo/redo buffers, countdown timers. | JavaFX 21, FXML, CSS3, HTML5, Vanilla JavaScript (ES6+). |
| **2. Controller / Gateway Tier** | Session verification, route handling, role authorization (`STUDENT` vs `ADMIN`), input validation. | JavaFX Controllers, Express.js middleware, `SessionManager`. |
| **3. Business & DSA Tier** | Coursework logic, contest scoring calculations, Top-K leaderboard sorting, asynchronous submission queues. | Java 17 OOP, `LeaderboardHeap`, `QuickSortRanking`, `SubmissionQueue`. |
| **4. Sandboxed Execution Tier** | Dynamic code compilation, subprocess isolation, memory and runtime bounds enforcement, diff validation. | `javax.tools.JavaCompiler`, `ProcessBuilder`, Watchdog threads. |
| **5. Data Persistence Tier** | Storage of users, problems, test cases, submissions, scores, battles, assignments, and audit trails. | JDBC, MySQL 8.0, Embedded H2 Database, Connection Pool. |

---

## 3. Component Hierarchy & Data Flow

```mermaid
graph LR
    subgraph UserInteraction["User Interaction"]
        User(["Student / Faculty"])
    end

    subgraph DesktopApp["JavaFX Client"]
        LC[LoginController]
        SMC[StudentMainController]
        ADC[AdminDashboardController]
        BAC[BattleArenaController]
        CEC[CodeEditorController]
    end

    subgraph CoreServices["Domain Services"]
        US[UserService]
        PS[ProblemService]
        BS[BattleService]
        SS[SubmissionService]
        AS[AssignmentService]
        LS[LeaderboardService]
    end

    subgraph DataAccess["JDBC DAO Layer"]
        UD[UserDAO]
        PD[ProblemDAO]
        TD[TestCaseDAO]
        BD[BattleDAO]
        SD[SubmissionDAO]
    end

    User -->|Authenticates| LC
    LC -->|Role: Student| SMC
    LC -->|Role: Admin / Faculty| ADC
    SMC --> BAC
    SMC --> CEC
    
    ADC & BAC & CEC --> CoreServices
    US --> UD
    PS --> PD & TD
    BS --> BD
    SS --> SD
    AS --> PD & SD
```

---

## 4. Sandboxed Java Code Execution Engine

Evaluating untrusted student-submitted source code requires strict process isolation, resource capping, and security screening to prevent system tampering, memory depletion, or denial-of-service (DoS) loops.

### Execution Sequence Workflow

```mermaid
sequenceDiagram
    autonumber
    actor Student as Student / Client
    participant API as Gateway / Controller
    participant Queue as SubmissionQueue (FIFO)
    participant Sec as SecurityScanner
    participant Comp as Dynamic Java Compiler
    participant Sandbox as Subprocess Sandbox
    participant Watchdog as Timeout Watchdog Thread
    participant Diff as Output Normalizer & Evaluator
    participant DB as Persistence Layer (Scores/Submissions)

    Student->>API: Submit Java Solution (Source Code + Problem ID)
    API->>Queue: Enqueue Submission
    Queue->>Sec: Dequeue & Scan Source Tokens
    alt Hazardous Pattern Detected (e.g. Runtime.exec, System.exit, ProcessBuilder)
        Sec-->>API: Reject with SECURITY_VIOLATION
        API-->>Student: Display Forbidden System Call Diagnostic
    else Clean Code
        Sec->>Comp: Transmit to Compiler (javax.tools / javac)
        alt Compilation Fails
            Comp-->>API: Emit COMPILATION_ERROR + Compiler Diagnostics
            API-->>Student: Display Syntax Error Log (Line Number & Token)
        else Compilation Succeeds
            Comp->>Sandbox: Spawn Process (java -cp . Solution)
            Sandbox->>Watchdog: Register 3000ms Watchdog Timer
            loop For Each Hidden & Sample Test Case
                Sandbox->>Sandbox: Inject Test Case STDIN
                Sandbox->>Sandbox: Capture Process STDOUT & STDERR
                alt Execution Exceeds 3000ms Limit
                    Watchdog->>Sandbox: Force Kill Process (process.destroyForcibly)
                    Sandbox-->>API: Emit TIME_LIMIT_EXCEEDED
                else Normal Termination
                    Sandbox->>Diff: Feed Output Strings
                    Diff->>Diff: Normalize Linebreaks (\r\n -> \n) & Trim
                    Diff->>Diff: Compare Actual vs Expected Outputs
                end
            end
            Diff->>DB: Store Submission Record & Calculate Awarded Points
            DB-->>API: Commit Success & Return Test Diagnostics
            API-->>Student: Return ACCEPTED (Score, Runtime ms, Tests Passed)
        end
    end
```

### Security Guardrails
1. **Source Token Scanning**: Before compiling, code is parsed through strict regular expressions filtering system manipulation:
   - `java.lang.reflect.*`
   - `java.lang.Runtime`
   - `java.lang.ProcessBuilder`
   - `System.exit()`
   - Arbitrary file I/O outside sandboxed working directory.
2. **Subprocess Isolation**: Student code runs in a distinct OS process created by `ProcessBuilder`, preventing heap pollution of the main application.
3. **Hardware Watchdogs**: A dedicated background thread monitors execution duration. If an execution exceeds 3000ms (preventing `while(true)` freezes), the thread forcefully invokes `destroyForcibly()`.

---

## 5. Real-Time Notification & Task Broadcast Engine

The Faculty / Admin Command Portal allows instructors to broadcast assignments, laboratory practical exams, and urgent coding missions instantly to every connected student dashboard.

```mermaid
flowchart TD
    Faculty["Faculty / Administrator"] -->|Creates Assignment / Exam / Task| CreateTask["POST /api/admin/assignments"]
    
    subgraph ServerEngine["Server Processing Engine"]
        CreateTask --> AssignLogic{"Target Audience Evaluation"}
        AssignLogic -->|"ALL"| TargetAll["Broadcast to All Active Students"]
        AssignLogic -->|"STUDENT:username"| TargetSingle["Target Specific Student"]
        AssignLogic -->|"CATEGORY / SEM"| TargetGroup["Target Class / Semester"]
        
        TargetAll & TargetSingle & TargetGroup --> NoticeFeed["Auto-Generate System Announcement"]
        TargetAll & TargetSingle & TargetGroup --> StoreRecord["Persist in Coursework Store"]
    end
    
    subgraph RealTimeSync["Live Sync & Push Mechanism"]
        NoticeFeed --> NotificationQueue["Broadcast Event Bus"]
        NotificationQueue --> PollingEngine["/api/student/live-broadcasts<br/>(Client Polling & Event Delivery)"]
    end

    subgraph StudentClients["Student Dashboards"]
        PollingEngine --> S1["Student A Dashboard<br/>(Pill Alert + Notification Banner)"]
        PollingEngine --> S2["Student B Dashboard<br/>(Pill Alert + Notification Banner)"]
        PollingEngine --> S3["Student C Dashboard<br/>(Pill Alert + Notification Banner)"]
    end
```

---

## 6. Resilient Dual-Mode Database Architecture

To guarantee **zero downtime and 100% presentation reliability** during lab evaluations or viva defense sessions, the platform features a self-healing dual-engine persistence layer.

```mermaid
stateDiagram-v2
    [*] --> InitializeConnection: Platform Startup
    InitializeConnection --> CheckMySQL: Read db.properties (db.mode=auto)
    
    state CheckMySQL {
        [*] --> AttemptMySQL: Attempt JDBC Handshake to localhost:3306
        AttemptMySQL --> MySQLConnected: Socket Connection OK
        AttemptMySQL --> MySQLFailed: Connection Refused / Timeout
    }
    
    MySQLConnected --> MySQLActive: Enable Production MySQL Mode (ACID)
    MySQLFailed --> FallbackH2: Trigger Auto-Failover Strategy
    
    state FallbackH2 {
        [*] --> BootEmbeddedH2: Initialize Embedded H2 Engine (./data/coding_battle_db)
        BootEmbeddedH2 --> EnableMySQLCompatibility: Apply MODE=MySQL
        EnableMySQLCompatibility --> H2Active: H2 Engine Online
    }
    
    MySQLActive --> ValidateTables: Run DDL Verification
    H2Active --> ValidateTables: Run DDL Verification
    
    ValidateTables --> SeedData: Check if records exist
    SeedData --> Ready: Platform Fully Initialized
    Ready --> [*]
```

### Relational Database Schema (ERD)

```mermaid
erDiagram
    USERS ||--o{ SUBMISSIONS : submits
    USERS ||--o{ SCORES : accumulates
    USERS ||--o{ ASSIGNMENT_SUBMISSIONS : completes
    PROBLEMS ||--|{ TEST_CASES : contains
    PROBLEMS ||--o{ SUBMISSIONS : evaluated_against
    BATTLES ||--o{ SCORES : generates
    ASSIGNMENTS ||--o{ ASSIGNMENT_SUBMISSIONS : receives

    USERS {
        int user_id PK
        string username
        string password
        string email
        string role
        string full_name
        timestamp created_at
    }

    PROBLEMS {
        int problem_id PK
        string title
        string description
        string difficulty
        string category
        int points
        int time_limit_ms
        int memory_limit_mb
    }

    TEST_CASES {
        int test_case_id PK
        int problem_id FK
        text input
        text expected_output
        boolean is_hidden
    }

    SUBMISSIONS {
        int submission_id PK
        int user_id FK
        int problem_id FK
        text code
        string status
        int execution_time_ms
        int memory_used_kb
        int tests_passed
        int total_tests
        timestamp submitted_at
    }

    BATTLES {
        int battle_id PK
        string title
        string description
        timestamp start_time
        timestamp end_time
        string status
    }

    SCORES {
        int score_id PK
        int user_id FK
        int battle_id FK
        int total_score
        int problems_solved
        int time_taken_seconds
    }

    ASSIGNMENTS {
        int assignment_id PK
        string title
        string type
        string category
        string assigned_to
        datetime due_date
        int max_marks
        string status
    }

    ASSIGNMENT_SUBMISSIONS {
        int id PK
        int assignment_id FK
        int user_id FK
        int marks_obtained
        string status
        timestamp submitted_at
    }
```

---

## 7. Design Patterns & Structural Blueprint

The platform strictly exemplifies established Software Engineering and Object-Oriented Design Patterns:

| Design Pattern | Implementation Location | Purpose & Architectural Justification |
| :--- | :--- | :--- |
| **Factory Pattern** | `CodeEvaluatorFactory` | Dynamically produces language-specific evaluators (Java, Python, C++) decoupled from the execution caller. |
| **Strategy Pattern** | `ScoreCalculator`, `DefaultScoreCalculator` | Encapsulates competitive scoring algorithms (e.g., speed decay, difficulty weightings, penalty deductions). |
| **Singleton Pattern** | `DatabaseConnection`, `SessionManager` | Guarantees a single shared connection pool and consistent user session state across multi-window views. |
| **Data Access Object (DAO)** | `UserDAO`, `ProblemDAO`, `SubmissionDAO`, etc. | Completely isolates raw SQL queries and relational persistence from the application's business layer. |
| **Model-View-Controller (MVC)** | JavaFX Controllers (`FXML` + Controllers + Models) | Strict decoupling of UI declarative state from domain logic and database interactions. |
| **Observer Pattern** | `NavigationCallback`, Live Notifications | Emits UI navigation transitions and real-time announcement updates across multiple listening views. |

---

## 8. Thread Safety, Concurrency & Process Watchdogs

1. **JavaFX Application Thread Safety**:
   - All background computation (compilation, database transactions, network I/O) executes on worker threads (`ExecutorService`).
   - UI updates are routed back to the main GUI thread strictly via `Platform.runLater()`, preventing `IllegalStateException: Not on FX application thread`.
2. **Submission Queue Concurrency**:
   - `SubmissionQueue` utilizes Java synchronization locks to safely ingest concurrent code submissions from multiple students without race conditions.
3. **Subprocess Watchdogs**:
   - Every execution subprocess is assigned an independent watchdog thread that performs a non-blocking `process.waitFor(3000, TimeUnit.MILLISECONDS)`. If timed out, `process.destroyForcibly()` ensures no orphaned threads or zombie processes consume server resources.

