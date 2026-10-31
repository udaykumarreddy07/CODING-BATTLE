# 📡 Coding Battle Platform — REST API Reference Manual

> **Gateway Version**: 2.5.0  
> **Base URL**: `http://localhost:3000/api` (or `http://localhost:8080/api` for Java HTTP Server)  
> **Data Format**: `application/json`  
> **Authentication**: Role-based session headers & credentials  

---

## 📑 Table of Contents
1. [Authentication & Account Endpoints](#1-authentication--account-endpoints)
2. [Problem Catalog & Curriculum Endpoints](#2-problem-catalog--curriculum-endpoints)
3. [Sandboxed Code Execution & Evaluation](#3-sandboxed-code-execution--evaluation)
4. [Live Coding Battles & Contests](#4-live-coding-battles--contests)
5. [Leaderboard & Gamification Endpoints](#5-leaderboard--gamification-endpoints)
6. [Faculty Command & Assignment Broadcast APIs](#6-faculty-command--assignment-broadcast-apis)

---

## 1. Authentication & Account Endpoints

### 1.1 User Login
Authenticates a student or administrator and returns role permissions and session context.

- **Endpoint**: `POST /api/auth/login`
- **Request Body**:
  ```json
  {
    "username": "admin",
    "password": "admin123"
  }
  ```
- **Response `200 OK`**:
  ```json
  {
    "success": true,
    "user": {
      "userId": 1,
      "username": "admin",
      "fullName": "Prof. Alexander Wright",
      "email": "admin.evaluator@codingbattle.edu",
      "role": "ADMIN",
      "designation": "Head of Academic Evaluation",
      "accessLevel": "SUPER_ADMIN"
    }
  }
  ```

### 1.2 Student Registration
Registers a new student into the active course cohort.

- **Endpoint**: `POST /api/auth/register`
- **Request Body**:
  ```json
  {
    "username": "priya_cse",
    "fullName": "Priya Sharma",
    "email": "priya.s@student.edu",
    "password": "password123",
    "rollNo": "CSE-2026-088",
    "dept": "Computer Science & Engineering",
    "semester": "Sem VI"
  }
  ```
- **Response `201 Created`**:
  ```json
  {
    "success": true,
    "message": "Student registered successfully.",
    "userId": 8
  }
  ```

---

## 2. Problem Catalog & Curriculum Endpoints

### 2.1 List Problems
Retrieves problems with optional filtering by difficulty, category, or search keywords.

- **Endpoint**: `GET /api/problems`
- **Query Parameters**:
  - `difficulty`: `EASY` | `MEDIUM` | `HARD`
  - `category`: `ARRAYS` | `STRINGS` | `OOP` | `RECURSION` | `DP`
  - `search`: Keyword string
- **Response `200 OK`**:
  ```json
  [
    {
      "problemId": 1,
      "title": "Two Sum",
      "difficulty": "EASY",
      "category": "ARRAYS",
      "points": 100,
      "timeLimitMs": 2000,
      "solvedStatus": "SOLVED"
    }
  ]
  ```

### 2.2 Retrieve Problem Details
Fetches complete problem specification including constraints and visible sample test cases.

- **Endpoint**: `GET /api/problems/:id`
- **Response `200 OK`**:
  ```json
  {
    "problemId": 1,
    "title": "Two Sum",
    "description": "Given an array of integers nums and an integer target, return indices of the two numbers...",
    "difficulty": "EASY",
    "points": 100,
    "starterCode": "public class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // Your code here\n    }\n}",
    "sampleTestCases": [
      {
        "input": "[2,7,11,15]\n9",
        "expectedOutput": "[0,1]"
      }
    ]
  }
  ```

---

## 3. Sandboxed Code Execution & Evaluation

### 3.1 Test Run Solution (Interactive Sandbox)
Compiles and runs code against sample test cases without persisting score.

- **Endpoint**: `POST /api/run`
- **Request Body**:
  ```json
  {
    "problemId": 1,
    "code": "public class Solution { public static void main(String[] args) { System.out.println(\"[0,1]\"); } }"
  }
  ```
- **Response `200 OK`**:
  ```json
  {
    "status": "ACCEPTED",
    "runtimeMs": 64,
    "passedTests": 2,
    "totalTests": 2,
    "stdout": "[0,1]\n"
  }
  ```

### 3.2 Formal Solution Submission
Submits code for evaluation against all visible and hidden test cases, computes contest points, updates streak, and records submission metrics.

- **Endpoint**: `POST /api/submit`
- **Request Body**:
  ```json
  {
    "problemId": 1,
    "userId": 2,
    "code": "public class Solution { ... }"
  }
  ```
- **Response `200 OK`**:
  ```json
  {
    "submissionId": 142,
    "status": "ACCEPTED",
    "testsPassed": 5,
    "totalTests": 5,
    "scoreAwarded": 100,
    "newTotalScore": 610,
    "rank": 1,
    "badgeEarned": "ARRAY_MASTER"
  }
  ```

---

## 4. Live Coding Battles & Contests

### 4.1 List Battles
- **Endpoint**: `GET /api/battles`
- **Response `200 OK`**:
  ```json
  [
    {
      "battleId": 1,
      "title": "Weekly Java Algorithm Clash #12",
      "startTime": "2026-11-01T10:00:00Z",
      "endTime": "2026-11-01T12:00:00Z",
      "status": "UPCOMING",
      "problemCount": 4,
      "registeredCount": 48
    }
  ]
  ```

---

## 5. Leaderboard & Gamification Endpoints

### 5.1 Global Ranking
Retrieves the ranked cohort ordered by the `QuickSortRanking` / `LeaderboardHeap` algorithms.

- **Endpoint**: `GET /api/leaderboard`
- **Response `200 OK`**:
  ```json
  [
    {
      "rank": 1,
      "userId": 2,
      "username": "rahul",
      "fullName": "Rahul Sharma",
      "totalScore": 610,
      "problemsSolved": 6,
      "badges": ["FIRST_BLOOD", "STREAK_7_DAYS"]
    }
  ]
  ```

---

## 6. Faculty Command & Assignment Broadcast APIs

### 6.1 Create & Broadcast Assignment / Exam / Task
Allows faculty to create an assignment, examination, or targeted coding mission, which is immediately pushed to student dashboards.

- **Endpoint**: `POST /api/admin/assignments`
- **Request Body**:
  ```json
  {
    "title": "Midterm Lab Exam: OOP & Exception Handling",
    "type": "EXAM",
    "category": "EXAMINATION",
    "priority": "URGENT",
    "description": "Solve the 2 assigned problem statements with full test coverage.",
    "problemIds": [1, 5],
    "assignedTo": "ALL",
    "dueDate": "2026-11-05T23:59:00",
    "durationMinutes": 90,
    "maxMarks": 50,
    "instructions": "No external libraries permitted. Strict 90-minute timer."
  }
  ```
- **Response `201 Created`**:
  ```json
  {
    "success": true,
    "message": "Assignment created and broadcasted to students successfully.",
    "assignmentId": 4
  }
  ```

### 6.2 Real-Time Student Broadcast Stream
Enables student dashboards to poll and receive newly assigned tasks and faculty announcements in real time.

- **Endpoint**: `GET /api/student/live-broadcasts`
- **Response `200 OK`**:
  ```json
  {
    "latestNotice": {
      "id": 18,
      "title": "URGENT: Midterm Lab Exam: OOP & Exception Handling",
      "type": "BROADCAST",
      "priority": "HIGH",
      "createdAt": "2026-10-31T09:30:00Z"
    },
    "totalActiveAssignments": 4,
    "timestamp": 1730347800000
  }
  ```

### 6.3 Review Student Submissions for an Assignment
- **Endpoint**: `GET /api/admin/assignments/:id/submissions`
- **Response `200 OK`**:
  ```json
  {
    "assignmentId": 1,
    "title": "Core Java Midterm Practical Examination",
    "totalStudents": 15,
    "submittedCount": 12,
    "submissions": [
      {
        "userId": 2,
        "studentName": "Rahul Sharma",
        "rollNo": "CSE-2026-001",
        "status": "COMPLETED",
        "autoGrade": 48,
        "maxMarks": 50,
        "submittedAt": "2026-10-30T14:22:00Z"
      }
    ]
  }
  ```

### 6.4 Manual Grade Adjustment & Faculty Feedback
- **Endpoint**: `POST /api/admin/assignments/:id/adjust-score`
- **Request Body**:
  ```json
  {
    "userId": 2,
    "newScore": 50,
    "feedbackNote": "Excellent optimization using dual pointers."
  }
  ```
- **Response `200 OK`**:
  ```json
  {
    "success": true,
    "message": "Grade updated and feedback recorded."
  }
  ```

### 6.5 Extend Assignment Deadline
- **Endpoint**: `POST /api/admin/assignments/:id/extend-deadline`
- **Request Body**:
  ```json
  {
    "additionalHours": 48
  }
  ```
- **Response `200 OK`**:
  ```json
  {
    "success": true,
    "newDueDate": "2026-11-07T23:59:00Z"
  }
  ```
