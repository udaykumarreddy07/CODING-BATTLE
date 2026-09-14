# Coding Battle Platform – Interactive Java Programming Competition System

A complete desktop-based competitive programming and coding battle platform developed with **JavaFX 21**, **Core Java & Object-Oriented Programming (OOP)**, **Data Structures & Algorithms (DSA)**, **JDBC**, and **MySQL** (with automated zero-config fallback to **Embedded H2** for viva/presentations).

---

## 🌟 Key Features

- **Dual User Roles**:
  - **Student Portal**: Practice curated DSA problems, compete in timed live coding battles, submit solutions, view test-case pass/fail diagnostics, earn points, climb global & battle leaderboards, track performance analytics with charts, and unlock gamification badges.
  - **Administrator Portal**: Complete CRUD control over coding problems, test cases, live/upcoming battles, student directories, and system-wide submission audit logs.
- **Sandboxed Java Code Evaluation**:
  - Dynamically compiles student Java code (`javax.tools.JavaCompiler` with fallback to `javac`).
  - Executes solutions in isolated processes with strict timeout watchdogs (preventing infinite loops and memory exhaustion).
  - Security scanner filtering hazardous system calls (`Runtime.getRuntime`, `ProcessBuilder`, `System.exit`, reflection).
  - Normalizes line endings (`\r\n` vs `\n`) and verifies output against sample and hidden test cases.
- **Data Structures & Algorithms (DSA)**:
  - **`LeaderboardHeap`**: Custom Max-Heap (PriorityQueue) extracting Top-K competitors in $O(K \log N)$ time.
  - **`QuickSortRanking`**: Custom QuickSort with median-of-three partitioning and tie-breakers in $O(N \log N)$ time.
  - **`EditorUndoStack`**: Custom Generic LIFO Stack powering code editor undo and redo in $O(1)$ time.
  - **`SubmissionQueue`**: Thread-safe FIFO Queue processing asynchronous solution runs.
  - **`ProblemSearchIndex`**: $O(1)$ `HashMap` lookup and $O(\log N)$ Binary Search for multi-attribute filtering.
- **Interactive Timed Battles**:
  - Live countdown timer (MM:SS) built with JavaFX `Timeline`.
  - Contest problem switcher ribbon with real-time solve indicators (✓).
  - Automatic contest finish and score calculation upon timer expiry.
- **Resilient Dual-Mode Database**:
  - Configured for **MySQL** (`jdbc:mysql://localhost:3306/coding_battle_db`).
  - Seamless auto-failover to **Embedded H2** (`./data/coding_battle_db`) if MySQL is not running or unconfigured, ensuring **zero presentation failures** during evaluation.

---

## 🏗️ Architecture & OOP Design

```
src/main/java/com/codingbattle/
│
├── model/                  # Domain Models (OOP Principles)
│   ├── User.java           # Abstract base class (Abstraction & Encapsulation)
│   ├── Student.java        # Inherits User (Inheritance & Polymorphism)
│   ├── Admin.java          # Inherits User (Inheritance & Polymorphism)
│   ├── Problem.java        # Problem definition (Composition with TestCases)
│   ├── TestCase.java       # Test case inputs and expected outputs
│   ├── Battle.java         # Contest model (Composition with Problems)
│   ├── Submission.java     # Student submission record & metrics
│   ├── Score.java          # Contest score record
│   ├── Achievement.java    # Gamification badge model
│   └── LeaderboardEntry.java # Implements Comparable for DSA sorting
│
├── dsa/                    # Data Structures & Algorithms Layer
│   ├── LeaderboardHeap.java    # Max-Heap / PriorityQueue Top-K algorithm
│   ├── QuickSortRanking.java   # Custom QuickSort with median-of-three pivot
│   ├── ProblemSearchIndex.java # HashMap + Binary Search over sorted arrays
│   ├── EditorUndoStack.java    # Custom LIFO Stack for Editor Undo/Redo
│   └── SubmissionQueue.java    # Thread-safe FIFO Queue
│
├── engine/                 # Sandboxed Execution & Scoring
│   ├── CodeEvaluator.java      # Abstraction Interface
│   ├── ScoreCalculator.java    # Abstraction Interface
│   ├── DefaultScoreCalculator.java # Weighted difficulty & speed bonus scoring
│   └── JavaCodeEvaluator.java  # Isolated compilation, process watchdog & diffing
│
├── dao/                    # Data Access Objects (JDBC Layer)
│   ├── UserDAO.java
│   ├── ProblemDAO.java
│   ├── TestCaseDAO.java
│   ├── BattleDAO.java
│   ├── SubmissionDAO.java
│   ├── ScoreDAO.java
│   └── AchievementDAO.java
│
├── database/               # Database Connection & Initialization
│   ├── DatabaseConnection.java  # Singleton with MySQL / H2 Failover
│   └── DatabaseInitializer.java # DDL execution & seed data population
│
├── service/                # Business Logic & Orchestration
│   ├── UserService.java
│   ├── ProblemService.java
│   ├── BattleService.java
│   ├── SubmissionService.java
│   ├── LeaderboardService.java
│   └── AchievementService.java
│
├── controller/             # JavaFX Controllers
│   ├── LoginController.java
│   ├── StudentMainController.java
│   ├── StudentDashboardController.java
│   ├── ProblemListController.java
│   ├── CodeEditorController.java
│   ├── BattleArenaController.java
│   ├── LeaderboardController.java
│   ├── StudentPerformanceController.java
│   └── AdminDashboardController.java
│
├── util/                   # Helpers & Utilities
│   ├── SessionManager.java
│   └── ValidationUtil.java
│
├── Main.java               # JavaFX Application class
└── AppLauncher.java        # Standard main entry point
```

---

## 🚀 Quick Start & How to Run

### Method 1: One-Click Windows Batch Script (Recommended)
Double-click `run.bat` in the project root, or execute:
```cmd
.\run.bat
```

### Method 2: PowerShell Launcher
```powershell
.\run.ps1
```

### Method 3: Standard Maven Command
```cmd
.\tools\apache-maven-3.9.16\bin\mvn.cmd javafx:run
```

---

## 🔑 Pre-Configured Demo Accounts

| Role | Username | Password | Notes |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `admin123` | Full access to Problem/Test Case/Battle CRUD and Audit logs |
| **Student** | `rahul` | `student123` | Has 5 solved problems, 510 points, Rank #1, and unlocked badges |
| **Student** | `ananya` | `student123` | Active student account |
| **Student** | `kiran` | `student123` | Active student account |

*(Or register a new account anytime on the Sign Up tab)*

---

## 🗄️ Database Configuration (MySQL & H2)

Configuration is located in `src/main/resources/db.properties`:
```properties
db.mode=auto
mysql.url=jdbc:mysql://localhost:3306/coding_battle_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
mysql.user=root
mysql.password=root

h2.url=jdbc:h2:./data/coding_battle_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=TRUE
```
- **Auto Mode**: The application first attempts to connect to your local MySQL instance. If MySQL is unavailable, it automatically starts the embedded **H2** database in MySQL compatibility mode.
- Both modes use the exact same tables, PreparedStatements, and seed data.

---

## 🧪 Pre-loaded Curated Algorithmic Problems

1. **Two Sum** (Arrays, Easy, 100 pts)
2. **Palindrome Number** (Mathematics, Easy, 100 pts)
3. **Valid Parentheses** (Stack, Easy, 100 pts)
4. **Reverse String** (Strings, Easy, 100 pts)
5. **Maximum Subarray / Kadane's Algorithm** (Dynamic Programming, Medium, 200 pts)
6. **Binary Search** (Searching, Easy, 100 pts)
7. **Nth Fibonacci Number** (Recursion, Easy, 100 pts)
8. **Longest Substring Without Repeating Characters** (Strings, Medium, 200 pts)

---

## 🎓 Viva Voce Questions & Model Answers

### Q1: How does your application demonstrate Object-Oriented Programming (OOP)?
- **Abstraction**: `User` is an abstract class defining core user contracts; `CodeEvaluator` and `ScoreCalculator` are interfaces decoupling business logic from concrete evaluation engines.
- **Inheritance**: `Student` and `Admin` extend `User`, inheriting shared fields (`username`, `email`, `password`) while adding role-specific attributes.
- **Polymorphism**: `getRoleDescription()` is overridden across `Student` and `Admin`.
- **Encapsulation**: All models feature `private` fields with strict validation in getters and setters.
- **Composition**: `Battle` contains a collection of `Problem` objects; `Problem` contains a collection of `TestCase` objects.

### Q2: Which Data Structures & Algorithms are used and what is their complexity?
1. **Max-Heap (`LeaderboardHeap`)**: Used to extract Top-K leaders dynamically. Insertion is $O(\log N)$, and extracting the Top $K$ leaders takes $O(K \log N)$, which is faster than full $O(N \log N)$ sorting when $K \ll N$.
2. **QuickSort (`QuickSortRanking`)**: Sorts the complete leaderboard by points descending with tie-breakers on problems solved. Time complexity is average $O(N \log N)$ with median-of-three pivot selection.
3. **LIFO Stack (`EditorUndoStack`)**: Custom generic dynamic array stack providing $O(1)$ push/pop for code editor undo/redo history.
4. **FIFO Queue (`SubmissionQueue`)**: Decouples UI actions from background compilation and process execution.
5. **Binary Search & HashMap (`ProblemSearchIndex`)**: $O(1)$ ID lookup and $O(\log N)$ binary search to find problems by exact points threshold.

### Q3: How do you safely evaluate student Java code without crashing the application?
- **Process Isolation**: The code is compiled to a temporary workspace and launched as an independent subprocess using `ProcessBuilder`.
- **Timeout Watchdog**: `Process.waitFor(timeLimit, TimeUnit.MILLISECONDS)` monitors execution. If the solution exceeds the timeout (e.g., an infinite `while(true)` loop), `process.destroyForcibly()` immediately terminates it and marks the submission as `TIME_LIMIT_EXCEEDED`.
- **Security Guard**: A regex scanner inspects the source code for forbidden operations (`Runtime.getRuntime`, `ProcessBuilder`, `System.exit`, reflection) before compilation.
