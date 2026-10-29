# ExamFlow — Smart Exam Scheduling System

ExamFlow is a full-stack application for generating and managing exam schedules using a Java Spring Boot backend and a React frontend.

## Tech Stack

- **Backend:** Java 21, Spring Boot, Maven
- **Frontend:** React, Vite, JavaScript
- **Database:** H2 (if configured in your application)

## Prerequisites

Install the following before running the application:

- Java 21 or compatible configured JDK
- Node.js and npm
- Git

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/Kandrumahalakshmi/smart-exam-flow_scheduler.git
cd smart-exam-flow_scheduler
```

### 2. Run the Backend

From the project root directory:

```bash
# Windows
mvnw.cmd spring-boot:run

# Git Bash / Linux / macOS
./mvnw spring-boot:run
```

Alternatively, if Maven is installed:

```bash
mvn spring-boot:run
```

The backend will start on the port configured in `src/main/resources/application.properties`. If no custom port is configured, Spring Boot normally uses `8080`.

### 3. Run the Frontend

Open a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Open the local URL printed by Vite in your terminal, commonly `http://localhost:5173`.

## Project Structure

```text
smart-exam-flow_scheduler/
├── src/          # Spring Boot backend source
├── data/         # Input data, if applicable
├── exports/      # Generated exports, if applicable
├── frontend/     # React frontend
├── pom.xml       # Maven configuration
├── mvnw          # Maven wrapper for Unix-like systems
└── README.md
```

## Features

Document the features implemented in your current version, such as:

- Exam scheduling
- Room and equipment management
- Scheduling validation
- Schedule export

## Notes

- Ensure the backend is running before using frontend features that call the API.
- Verify that the frontend API base URL matches the backend URL.
- Update this README if your database configuration, ports, or run commands differ from the defaults above.
