# Order Returns System

A full-stack web application for submitting product return requests, validating eligibility against business rules, and automatically approving or rejecting returns.

## Project Overview

The **Order Returns System** allows customers to submit return requests for purchased products. The application evaluates each request based on a 30-day return window and item condition, then stores the decision with explanatory remarks. Users can view individual return status and browse all submitted requests through a Bootstrap-powered UI.

## Features

- **Submit Return Request** — Web form for Order ID, Product Name, Purchase Date, Return Reason, and Item Condition
- **Automatic Validation** — Business rules applied when admin approves a request:
  - Returns allowed within 30 days of purchase
  - NEW and GOOD conditions are eligible
  - DAMAGED items are rejected
- **Pending Review Workflow** — User submissions start as `PENDING`; admin reviews and approves or rejects
- **Status Display** — Shows Return Request ID, Order ID, Product Name, Status, Remarks, and Submitted Date
- **View All Requests** — Bootstrap table listing all returns with color-coded status badges
- **Authentication & Authorization** — Spring Security login/register with **User** and **Admin** roles
  - Users see only their own returns
  - Admins see all returns across users
- **Bean Validation** — Server-side validation with user-friendly error messages
- **Exception Handling** — Global handler for not-found and unexpected errors
- **User Registration** — Customers sign up at `/register`; no pre-created customer accounts

## Tech Stack

| Layer        | Technology           |
|--------------|----------------------|
| Language     | Java 17              |
| Framework    | Spring Boot 3.x      |
| Security     | Spring Security      |
| Persistence  | Spring Data MongoDB  |
| Database     | MongoDB              |
| Templates    | Thymeleaf            |
| UI           | Bootstrap 5          |
| Build        | Maven                |
| Testing      | JUnit 5, Mockito     |

## Setup Instructions

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- MongoDB 4.4+ running locally (default: `localhost:27017`)

### Install and Start MongoDB

1. Download and install [MongoDB Community Server](https://www.mongodb.com/try/download/community) for Windows.
2. During setup, choose **Install MongoDB as a Service** (default port `27017`).
3. Start MongoDB:

```powershell
net start MongoDB
```

If MongoDB is already running as a Windows service, it starts automatically with your machine.

### Clone and Build

```bash
cd project_ANSR
mvn clean install
```

## Run Instructions

1. Ensure MongoDB is running on `localhost:27017`
2. Start the application:

```bash
mvn spring-boot:run
```

Open your browser and navigate to:

```
http://127.0.0.1:8080/login
```

You will be redirected to login if not authenticated.

### Getting Started

1. **Customers** — Go to `/register`, create an account, then log in and submit returns.
2. **Administrator** — One system admin account exists for first-time setup (see below). Use it to review customer returns.

### Administrator Account (system only)

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |

Customers are **not** pre-created. Every customer must **sign up** at `/register`.

### User vs Admin

| Feature | User | Admin |
|---------|------|-------|
| Submit return | Yes | No |
| View own returns | Yes (`/my-returns`) | No |
| Manage all returns | No | Yes (`/returns`) |
| Review pending requests | No | Yes |
| Approve / reject returns | No | Yes |
| View any return detail | Own only | Yes |

### Available Routes

| Method | URL              | Description                    |
|--------|------------------|--------------------------------|
| GET    | `/login`         | Sign in                        |
| GET    | `/register`      | Create user account            |
| GET    | `/`              | Submit return form (users only) |
| POST   | `/returns`       | Submit a return request (users only) |
| GET    | `/my-returns`    | My returns (user only) |
| GET    | `/returns`       | Manage returns (admin only) |
| GET    | `/returns/{id}`  | View a specific return status  |

## MongoDB Access

The application connects to:

| Setting    | Value                              |
|------------|------------------------------------|
| URI        | `mongodb://localhost:27017/returnsdb` |
| Database   | `returnsdb`                        |
| Collection | `return_requests`                  |

Inspect data with MongoDB Compass or the shell:

```bash
mongosh returnsdb
db.return_requests.find().pretty()
```

### Custom MongoDB Connection

Override in `application.properties` or via environment variable:

```properties
spring.data.mongodb.uri=mongodb://username:password@host:27017/returnsdb
```

## Troubleshooting

### Application fails to start — MongoDB connection refused

- Start MongoDB before running the app
- Verify the URI in `application.properties` matches your MongoDB host and port

### Port 8080 already in use

Change the port in `application.properties`:

```properties
server.port=8081
```

## Assumptions

- **Authentication is required** — users must log in to submit or view returns.
- The return window is **30 days** from the purchase date.
- Items in **DAMAGED** condition are not eligible for return.
- **Users** submit return requests (status starts as `PENDING`) and can only view their own returns.
- **Admins** review pending requests and approve or reject them; business rules apply on approval.
- Registration creates **USER** role only; each customer signs up with a unique username.
- No sample return data is preloaded — returns appear only after customers submit requests.
- Only the **admin** system account is created at startup; all customers register themselves.
- Purchase dates in the future are rejected at the form validation level.
- MongoDB is available at `localhost:27017` by default.

## AI Usage

ChatGPT and Cursor were used during development for:

- Architecture planning and layer separation (Entity, Repository, Service, Controller)
- Boilerplate generation (Maven `pom.xml`, Spring Boot configuration, MongoDB document model)
- Validation logic and business rule implementation
- Thymeleaf template and Bootstrap UI scaffolding
- Unit test structure and README documentation

## Project Structure

```
src/main/java/com/orderrreturns/
├── OrderReturnsApplication.java
├── config/
│   ├── DataInitializer.java
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   └── ReturnController.java
├── dto/
│   ├── RegisterDto.java
│   └── ReturnRequestDto.java
├── entity/
│   ├── ItemCondition.java
│   ├── ReturnRequest.java
│   ├── ReturnStatus.java
│   ├── Role.java
│   └── User.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── repository/
│   ├── ReturnRequestRepository.java
│   └── UserRepository.java
└── service/
    ├── CustomUserDetailsService.java
    ├── ReturnService.java
    └── UserService.java

src/main/resources/
├── application.properties
├── static/css/app.css
└── templates/
    ├── fragments/layout.html
    ├── index.html
    ├── login.html
    ├── register.html
    ├── status.html
    ├── returns.html
    └── error.html
```
