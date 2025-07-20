# Task Tracking and Management Backend System

## Project Overview

This is the backend system for a collaborative task tracking and management application. It provides RESTful APIs for user authentication, task management, team/project collaboration, and real-time notifications. The system is built using Spring Boot, Gradle, and PostgreSQL.

## Features

* **User Management:**
    * Secure user registration, login, and profile management.
    * JWT-based authentication for secure API access.
* **Task Management:**
    * Create, Read, Update, Delete (CRUD) tasks.
    * Task filtering, sorting, and searching.
    * Mark tasks as completed.
* **Team/Project Collaboration:**
    * Create and join teams/projects.
    * Assign tasks to team members.
    * Add comments to tasks.
    * Attach files to tasks.
* **Real-time Notifications:**
    * Instant updates for task assignments and changes using WebSockets.
* **Generative AI Integration (Optional/Planned):**
    * Automatically generate task descriptions or summaries.

## Technologies Used

* **Backend:** Java 17+, Spring Boot 3.x
* **Build Tool:** Gradle
* **Database:** PostgreSQL
* **Authentication:** Spring Security, JWT (JJWT)
* **Real-time:** Spring WebSockets
* **Data Access:** Spring Data JPA
* **Testing:** JUnit 5, Mockito

## Getting Started

### Prerequisites

* Java Development Kit (JDK) 17 or higher
* Gradle 8.x or higher
* PostgreSQL database server
* (Optional) Docker for easy database setup

### Database Setup

1.  **Install PostgreSQL:** If you don't have PostgreSQL installed, you can download it from [postgresql.org](https://www.postgresql.org/download/).
2.  **Create Database:** Create a new database for the application.
    ```sql
    CREATE DATABASE task_tracker_db;
    CREATE USER task_tracker_user WITH PASSWORD 'your_secure_password';
    GRANT ALL PRIVILEGES ON DATABASE task_tracker_db TO task_tracker_user;
    ```
3.  **Update `application.properties`:** Configure your database connection in `src/main/resources/application.properties`.

    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/task_tracker_db
    spring.datasource.username=task_tracker_user
    spring.datasource.password=your_secure_password
    spring.datasource.driver-class-name=org.postgresql.Driver
    spring.jpa.hibernate.ddl-auto=update # or validate in production
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.format_sql=true
    ```

### Running the Application

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/your-username/task-tracker-backend.git](https://github.com/your-username/task-tracker-backend.git)
    cd task-tracker-backend
    ```
2.  **Build the project:**
    ```bash
    ./gradlew clean build
    ```
3.  **Run the application:**
    ```bash
    ./gradlew bootRun
    ```
    The application will start on `http://localhost:8080` by default.

## API Documentation (Postman/Swagger)

Once the application is running, you can access the API endpoints. Consider using:

* **Swagger UI (Springdoc OpenAPI):** Add `implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.x.x'` to `build.gradle` and access at `http://localhost:8080/swagger-ui.html`.
* **Postman Collection:** A Postman collection will be provided (or created by you) to easily test the endpoints.

## Project Structure

src/main/java/com/tasktracker

├── config             # Spring Security, Web, WebSocket configurations

├── controller         # REST API endpoints

├── dto                # Data Transfer Objects (request/response)

├── entity             # JPA entities (data model)

├── exception          # Custom exceptions and global handlers

├── mapper             # MapStruct mappers (optional)

├── repository         # Spring Data JPA repositories

├── service            # Business logic, core functionality

├── security           # JWT utilities, filters, user details service

└── util               # Utility classes (e.g., file storage)