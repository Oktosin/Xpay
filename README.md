# XPay

A secure digital banking and payment platform built with Spring Boot, designed to support customer onboarding, account management, fund transfers, card issuance, card payments, and transaction processing.

> **Project Status:** 🚧 Under Active Development

---

## Overview

XPay is a backend payment and banking system that provides APIs for:

* Customer registration and management
* Staff management
* User authentication
* Account operations
* Money transfers
* Deposits and withdrawals
* Transaction reversals
* Virtual/physical card issuance
* Card authorization and payment processing
* Transaction history retrieval
* Password reset workflows

The project follows a layered architecture using:

* Spring Boot
* Spring Security
* Spring Data JPA
* Hibernate
* Microsoft SQL Server
* Maven
* Lombok

---

## Features

### Authentication & Security

* User login
* User logout
* Password reset with OTP verification
* Spring Security integration
* Session management
* Custom authentication handlers
* Access control
* Request rate limiting

### Customer Management

* Create customer accounts
* Retrieve customer information
* List all customers

### Staff Management

* Create staff accounts
* Retrieve staff information
* List all staff

### Transaction Processing

* Account-to-account transfers
* Cash deposits
* Cash withdrawals
* Transaction reversals
* Transaction history lookup
* Idempotency support for safe retries

### Card Services

* Card issuance
* Card status updates
* Payment authorization
* Payment capture
* Card payments
* Card transaction reversal
* Automatic release of expired authorization holds
* Card transaction history

---

## Technology Stack

| Technology           | Purpose                        |
| -------------------- | ------------------------------ |
| Java 17              | Programming Language           |
| Spring Boot 4        | Application Framework          |
| Spring Security      | Authentication & Authorization |
| Spring Data JPA      | Data Persistence               |
| Hibernate            | ORM                            |
| Microsoft SQL Server | Database                       |
| Maven                | Dependency Management          |
| Lombok               | Boilerplate Reduction          |

---

## Project Structure

```text
src/main/java/com/tosin/xpay

├── controller
│   ├── AuthController
│   ├── CustomerController
│   ├── StaffController
│   ├── TransactionController
│   ├── CardTransactionController
│   └── UserDataController
│
├── service
├── dao
├── dto
├── model
├── security
└── constant
```

---

## Database Configuration

The application is configured to use Microsoft SQL Server.

---

## Running the Application

### Prerequisites

* Java 17+
* Maven 3.9+
* SQL Server

### Clone the Repository

```bash
git clone https://github.com/Oktosin/xpay.git

cd xpay
```

### Build

```bash
mvn clean install
```

### Run

```bash
mvn spring-boot:run
```

The application starts on:

```text
http://localhost:8080/api
```

---

# API Endpoints

Base URL:

```text
http://localhost:8080/api
```

---

## Authentication

### Login

```http
POST /api/auth/login
```

### Logout

```http
POST /api/auth/logout
```

### Request Password Reset OTP

```http
POST /api/auth/password-reset/request-otp
```

### Confirm Password Reset

```http
POST /api/auth/password-reset/confirm
```

---

## Customer APIs

### Create Customer

```http
POST /api/customer/create
```

Example Request:

```json
{
  "username": "john",
  "password": "password123",
  "email": "john@example.com",
  "phoneNumber": "08012345678",
  "firstName": "John",
  "lastName": "Doe",
  "balance": 10000
}
```

### Find Customer by Account Number

```http
GET /api/customer/find-by-account-number
```

### Get All Customers

```http
GET /api/customer/find-all
```

---

## Staff APIs

### Create Staff

```http
POST /api/staff/create
```

### Find Staff

```http
GET /api/staff/find
```

### Get All Staff

```http
GET /api/staff/find-all
```

---

## Transaction APIs

### Transfer Funds

```http
POST /api/transaction/transfer
```

Example Request:

```json
{
  "senderAccountNumber": "100001",
  "receiverAccountNumber": "100002",
  "amount": 5000,
  "idempotencyKey": "txn-001",
  "description": "Transfer"
}
```

### Deposit Funds

```http
POST /api/transaction/deposit
```

Example Request:

```json
{
  "accountNumber": "100001",
  "amount": 10000,
  "idempotencyKey": "dep-001",
  "description": "Cash Deposit"
}
```

### Withdraw Funds

```http
POST /api/transaction/withdraw
```

Example Request:

```json
{
  "accountNumber": "100001",
  "amount": 2000,
  "idempotencyKey": "wd-001",
  "description": "ATM Withdrawal"
}
```

### Reverse Transaction

```http
POST /api/transaction/reverse
```

### Transaction History

```http
GET /api/transaction/history/{accountNumber}
```

Example:

```http
GET /api/transaction/history/100001
```

---

## Card APIs

### Issue Card

```http
POST /api/card/issue
```

Example Request:

```json
{
  "accountNumber": "100001",
  "cardNumber": "4111111111111111",
  "cardHolderName": "John Doe",
  "expiryDate": "2030-12-31",
  "pin": "1234",
  "cvv": "123",
  "transactionLimit": 50000,
  "dailyLimit": 200000,
  "monthlyLimit": 1000000
}
```

### Update Card Status

```http
POST /api/card/status
```

### Authorize Card Transaction

```http
POST /api/card/authorize
```

### Capture Authorized Funds

```http
POST /api/card/capture
```

### Process Card Payment

```http
POST /api/card/payment
```

### Reverse Card Transaction

```http
POST /api/card/reverse
```

### Release Expired Authorization Holds

```http
POST /api/card/release-expired-authorizations
```

### Card Transaction History

```http
GET /api/card/history/{accountNumber}
```

Example:

```http
GET /api/card/history/100001
```

---

## User Data APIs

### Find User

```http
GET /api/user-data/find-user
```

---

## Core Domain Models

### Customer

Represents a bank customer and account owner.

### Account

Contains:

* Account Number
* First Name
* Last Name
* Current Balance

### Transaction

Contains:

* Reference Number
* Sender Account
* Receiver Account
* Amount
* Description
* Reversal Information
* Creation Timestamp

### Card

Contains:

* Card Number (hashed)
* PIN (hashed)
* CVV (hashed)
* Expiry Date
* Spending Limits
* Hold Balance

---

## Security

Current security features include:

* Spring Security integration
* Authentication entry points
* Access denied handlers
* Session expiration handling
* Rate limiting filter
* Password reset workflow
* Credential protection

---

## Roadmap

Planned enhancements include:

* JWT Authentication
* Refresh Tokens
* API Documentation (Swagger/OpenAPI)
* Notification Service
* Email Verification
* Audit Logging
* Merchant APIs
* Wallet Services
* Payment Gateway Integrations
* Multi-Currency Support
* Docker Deployment
* CI/CD Pipeline

---

## Contributing

Contributions, suggestions, and issue reports are welcome.

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push your branch
5. Open a Pull Request

---

## License

License information will be added before the first stable release.

---

## Disclaimer

This project is currently under active development and APIs may change without notice until the first stable release.

---

Author

Tosin Okuwobi

Backend Engineer | Java & Spring Boot Developer

Focused on building scalable backend systems, fintech platforms, and enterprise software solutions.
