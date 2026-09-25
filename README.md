# 🔐 Learn Spring Security

> A practical Spring Security project created **just for learning and understanding how Spring Security works internally**.

This project focuses on understanding the complete authentication pipeline instead of simply copying Spring Security configuration from tutorials.

---

# 📚 Table of Contents

1. [Project Goal](#-project-goal)
2. [Big Picture](#-big-picture)
3. [Authentication vs Authorization](#-authentication-vs-authorization)
4. [Spring Security Core Concepts](#-spring-security-core-concepts)
5. [UserDetailsService](#-userdetailsservice)
6. [UserDetails](#-userdetails)
7. [PasswordEncoder](#-passwordencoder)
8. [Password Hashing vs JWT Signing](#-password-hashing-vs-jwt-signing)
9. [Password Encoders](#-password-encoders)

    * [NoOp](#1-noop)
    * [BCrypt](#2-bcrypt)
    * [SCrypt](#3-scrypt)
    * [Argon2](#4-argon2)
10. [Password Encoder Comparison](#-password-encoder-comparison)
11. [Project Architecture](#-project-architecture)
12. [File-by-File Explanation](#-file-by-file-explanation)
13. [Registration Flow](#-registration-flow)
14. [Login Flow](#-login-flow)
15. [AuthenticationManager](#-authenticationmanager)
16. [DaoAuthenticationProvider](#-daoauthenticationprovider)
17. [CustomUserDetailsService](#-customuserdetailsservice)
18. [JWT](#-jwt)
19. [JWT Structure](#-jwt-structure)
20. [JWT Signing Algorithms](#-jwt-signing-algorithms)
21. [Symmetric Cryptography — HMAC](#-symmetric-cryptography--hmac)
22. [Asymmetric Cryptography](#-asymmetric-cryptography)
23. [RSA](#-rsa)
24. [ECDSA](#-ecdsa)
25. [RSA vs ECDSA](#-rsa-vs-ecdsa)
26. [HMAC vs RSA vs ECDSA](#-hmac-vs-rsa-vs-ecdsa)
27. [JwtService](#-jwtservice)
28. [JwtAuthenticationFilter](#-jwtauthenticationfilter)
29. [SecurityContext](#-securitycontext)
30. [Complete Login Architecture](#-complete-login-architecture)
31. [Complete JWT Request Architecture](#-complete-jwt-request-architecture)
32. [Important Security Rules](#-important-security-rules)
33. [Complete Component Reference](#-complete-component-reference)
34. [Learning Order](#-learning-order)
35. [Final Mental Model](#-final-mental-model)

---

# 🎯 Project Goal

The goal of this project is **not simply to implement login**.

The goal is to understand what happens internally when a user:

1. Registers
2. Provides a password
3. Has the password securely hashed
4. Stores the password hash in the database
5. Logs in
6. Gets authenticated by Spring Security
7. Receives a JWT
8. Sends the JWT with another request
9. Gets the JWT verified
10. Gets an `Authentication` object
11. Reaches a protected controller

The complete process can be divided into two major security systems:

```text
                    APPLICATION SECURITY
                           │
              ┌────────────┴────────────┐
              │                         │
              ▼                         ▼
       PASSWORD AUTHENTICATION       JWT AUTHENTICATION
              │                         │
              ▼                         ▼
       PasswordEncoder              JWT Signature
              │                         │
       ┌──────┴──────┐           ┌──────┴──────┐
       ▼             ▼           ▼             ▼
    BCrypt        SCrypt      HMAC          RSA/ECDSA
       │             │           │             │
       └──────┬──────┘           └──────┬──────┘
              │                         │
              ▼                         ▼
        Password Hash              JWT Signature
```

---

# 🧠 Big Picture

A very important concept in this project is that **password security and JWT security are two different things**.

### Password security

```text
Password
    │
    ▼
PasswordEncoder
    │
    ▼
Hash
    │
    ▼
Database
```

Examples:

```text
BCrypt
SCrypt
Argon2
```

### JWT security

```text
JWT
 │
 ▼
Cryptographic Signing
 │
 ├── HMAC
 ├── RSA
 └── ECDSA
```

So:

```text
PasswordEncoder
      ≠
JWT signing algorithm
```

For example, you can use:

```text
Argon2 + HMAC
Argon2 + RSA
Argon2 + ECDSA
```

The password encoder and JWT algorithm are independent choices.

---

# 🔐 Authentication vs Authorization

## Authentication

Authentication answers:

> **Who are you?**

Example:

```text
username + password
        │
        ▼
Authentication
        │
        ▼
User identified
```

---

## Authorization

Authorization answers:

> **What are you allowed to do?**

Example:

```text
Authenticated User
        │
        ▼
Role: USER
        │
        ▼
Can access:
GET /profile

Cannot access:
DELETE /admin/users
```

Therefore:

```text
Authentication = Who are you?
Authorization  = What can you access?
```

---

# 🔐 Spring Security Core Concepts

The main Spring Security components used in this project are:

```text
AuthenticationManager
        │
        ▼
DaoAuthenticationProvider
        │
        ▼
UserDetailsService
        │
        ▼
UserDetails
        │
        ▼
PasswordEncoder
```

After successful authentication:

```text
Authentication Success
        │
        ▼
JwtService
        │
        ▼
JWT
```

For subsequent requests:

```text
JWT
 │
 ▼
JwtAuthenticationFilter
 │
 ▼
Signature Verification
 │
 ▼
SecurityContext
 │
 ▼
Controller
```

---

# 👤 UserDetailsService

`UserDetailsService` is a Spring Security interface responsible for **loading user security information**.

Spring Security needs information such as:

* username
* password
* authorities
* roles
* account status
* account locked status
* account expiration

Spring Security does not know how our application stores users.

Our application may use:

```text
PostgreSQL
MySQL
MongoDB
LDAP
External API
```

Therefore, we implement:

```java
@Service
public class CustomUserDetailsService
        implements UserDetailsService {
```

Its job is:

```text
Spring Security
       │
       ▼
UserDetailsService
       │
       ▼
CustomUserDetailsService
       │
       ▼
UserRepository
       │
       ▼
Database
```

---

# 👤 UserDetails

`UserDetails` is the interface Spring Security uses to represent a user's security information.

It can contain:

```text
Username
Password
Authorities
Account enabled
Account locked
Account expired
Credentials expired
```

Example:

```java
UserDetails userDetails =
        User.withUsername("rajat")
                .password(password)
                .roles("USER")
                .build();
```

The distinction is:

```text
UserEntity
    │
    └── Application's representation of user


UserDetails
    │
    └── Spring Security's representation of user
```

---

# 🔑 PasswordEncoder

`PasswordEncoder` is a Spring Security interface used to:

1. Encode/hash passwords.
2. Verify a raw password against a stored password hash.

Example:

```java
passwordEncoder.encode(password);
```

And during authentication:

```java
passwordEncoder.matches(
    rawPassword,
    storedHash
);
```

The important point:

> Password encoders are for **password hashing**, not JWT generation.

---

# 🔐 Password Hashing vs JWT Signing

These two concepts should never be confused.

## Password hashing

```text
Plain Password
      │
      ▼
Password Encoder
      │
      ▼
Password Hash
      │
      ▼
Database
```

The purpose is to securely store a password.

Examples:

```text
BCrypt
SCrypt
Argon2
```

---

## JWT signing

```text
JWT Data
   │
   ▼
Signing Algorithm
   │
   ▼
Digital Signature
   │
   ▼
Signed JWT
```

Examples:

```text
HMAC
RSA
ECDSA
```

Therefore:

```text
BCrypt / SCrypt / Argon2
        ↓
Password protection


HMAC / RSA / ECDSA
        ↓
JWT signing and verification
```

---

# 🔐 Password Encoders

This project explores three important password hashing algorithms:

```text
BCrypt
SCrypt
Argon2
```

---

# 1. NoOp

Spring Security can represent an unencoded password using:

```text
{noop}password
```

For example:

```text
{noop}ourpassword
```

This means:

> Do not apply password hashing.

The stored value is effectively treated as the raw password.

### Important

This is useful for:

* basic demonstrations
* temporary learning
* simple testing

It should **not** be used for real password storage.

---

# 2. BCrypt

BCrypt is a password hashing algorithm designed to make password guessing more expensive.

Example configuration:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Flow:

```text
ourpassword
      │
      ▼
BCryptPasswordEncoder
      │
      ▼
BCrypt Hash
      │
      ▼
Database
```

During login:

```text
Entered Password
      │
      ▼
BCryptPasswordEncoder.matches()
      │
      ▼
Stored BCrypt Hash
      │
      ▼
true / false
```

BCrypt is:

* salted
* deliberately computationally expensive
* designed for password hashing
* one-way

You do not decrypt a BCrypt password.

You verify it.

---

# 3. SCrypt

SCrypt is another password hashing / key derivation algorithm designed to make password cracking more expensive.

Spring Security provides:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return SCryptPasswordEncoder
            .defaultsForSpringSecurity_v5_8();
}
```

Flow:

```text
ourpassword
      │
      ▼
SCryptPasswordEncoder
      │
      ▼
SCrypt Hash
      │
      ▼
Database
```

SCrypt is designed to be **memory-hard**.

That means it intentionally requires significant memory resources in addition to computational work.

This makes large-scale password cracking more expensive.

---

# 4. Argon2

Argon2 is a modern password hashing algorithm designed to resist password cracking attacks.

Spring Security provides:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return Argon2PasswordEncoder
            .defaultsForSpringSecurity_v5_8();
}
```

Flow:

```text
ourpassword
      │
      ▼
Argon2PasswordEncoder
      │
      ▼
Argon2 Hash
      │
      ▼
Database
```

Argon2 is also designed to be memory-hard.

There are different Argon2 variants, including:

```text
Argon2d
Argon2i
Argon2id
```

Argon2id is commonly associated with password hashing because it combines properties intended to provide strong resistance against different attack techniques.

---

# 🔄 BCrypt vs SCrypt vs Argon2

| Feature                  | BCrypt           | SCrypt           | Argon2           |
| ------------------------ | ---------------- | ---------------- | ---------------- |
| Purpose                  | Password hashing | Password hashing | Password hashing |
| One-way                  | Yes              | Yes              | Yes              |
| Salted                   | Yes              | Yes              | Yes              |
| Memory-hard              | No               | Yes              | Yes              |
| Configurable work        | Yes              | Yes              | Yes              |
| Spring `PasswordEncoder` | Yes              | Yes              | Yes              |
| JWT generation           | No               | No               | No               |
| Encryption               | No               | No               | No               |

---

# 🧠 Password Encoder Migration

Because the application depends on:

```java
PasswordEncoder
```

we can replace the implementation.

For example:

```text
PasswordEncoder
      │
      ├── BCryptPasswordEncoder
      │
      ├── SCryptPasswordEncoder
      │
      └── Argon2PasswordEncoder
```

The rest of the authentication architecture does not need to know which implementation is being used.

For this project, the progression is:

```text
BCrypt
   ↓
SCrypt
   ↓
Argon2
```

If existing users have passwords encoded using one algorithm, their stored hashes cannot simply be interpreted as hashes from another algorithm.

For a learning project, test users can be recreated after changing the encoder.

---

# 🏗️ Project Architecture

```text
src/
└── main/
    ├── java/
    │   └── com/
    │       └── example/
    │           └── spring_security_demo/
    │
    │               ├── config/
    │               │   ├── SecurityConfig.java
    │               │   └── JwtKeyConfig.java
    │               │
    │               ├── controller/
    │               │   └── AuthController.java
    │               │
    │               ├── dto/
    │               │   ├── LoginRequestDTO.java
    │               │   └── LoginResponseDTO.java
    │               │
    │               ├── entity/
    │               │   └── UserEntity.java
    │               │
    │               ├── repository/
    │               │   └── UserRepository.java
    │               │
    │               ├── service/
    │               │   ├── AuthService.java
    │               │   └── AuthServiceImpl.java
    │               │
    │               └── security/
    │                   ├── CustomUserDetailsService.java
    │                   ├── JwtService.java
    │                   └── JwtAuthenticationFilter.java
    │
    └── resources/
        ├── application.properties
        │
        └── keys/
            ├── private_key.pem
            └── public_key.pem
```

---

# 📂 File-by-File Explanation

## `SecurityConfig.java`

Main Spring Security configuration.

Responsibilities:

* Configure `SecurityFilterChain`
* Configure public/protected endpoints
* Configure `PasswordEncoder`
* Configure `AuthenticationProvider`
* Configure `AuthenticationManager`
* Register JWT filter
* Configure stateless authentication

Conceptually:

```text
SecurityConfig
    │
    ├── PasswordEncoder
    ├── AuthenticationProvider
    ├── AuthenticationManager
    ├── SecurityFilterChain
    └── JwtAuthenticationFilter
```

---

# `JwtKeyConfig.java`

Responsible for loading or creating the cryptographic keys used by JWT.

For asymmetric algorithms:

```text
private_key.pem
      │
      ▼
PrivateKey


public_key.pem
      │
      ▼
PublicKey
```

Private key:

```text
SIGN
```

Public key:

```text
VERIFY
```

For HMAC, this class is not needed in the same private/public-key form because HMAC uses one shared secret.

---

# `AuthController.java`

The HTTP/API layer.

Example endpoints:

```http
POST /auth/register
POST /auth/login
```

Flow:

```text
HTTP Request
     │
     ▼
AuthController
     │
     ▼
AuthService
```

The controller should remain relatively thin.

---

# `LoginRequestDTO.java`

Carries login input.

Example:

```json
{
    "username": "rajat",
    "password": "ourpassword"
}
```

---

# `LoginResponseDTO.java`

Carries login output.

Example:

```json
{
    "token": "eyJhbGciOi..."
}
```

---

# `UserEntity.java`

Represents the application user stored in the database.

Example:

```text
UserEntity
├── id
├── username
└── password
```

The password field should contain a **password hash**, not the original password.

---

# `UserRepository.java`

Responsible for database access.

Example:

```java
Optional<UserEntity> findByUsername(String username);
```

Flow:

```text
CustomUserDetailsService
        │
        ▼
UserRepository
        │
        ▼
PostgreSQL
```

---

# `AuthService.java`

Defines authentication-related business operations.

Example:

```java
void register(RegisterRequestDTO request);

LoginResponseDTO login(LoginRequestDTO request);
```

---

# `AuthServiceImpl.java`

Implements authentication business logic.

Registration:

```text
Request
  │
  ▼
AuthServiceImpl
  │
  ▼
PasswordEncoder
  │
  ▼
UserRepository
  │
  ▼
Database
```

Login:

```text
Login Request
      │
      ▼
AuthenticationManager
      │
      ▼
Authentication
      │
      ▼
JwtService
      │
      ▼
JWT
```

---

# `CustomUserDetailsService.java`

Connects our application's database with Spring Security.

```text
username
   │
   ▼
CustomUserDetailsService
   │
   ▼
UserRepository
   │
   ▼
Database
   │
   ▼
UserEntity
   │
   ▼
UserDetails
```

It implements:

```java
UserDetailsService
```

---

# `JwtService.java`

Responsible for JWT operations.

Depending on the selected algorithm, it can:

* create JWTs
* sign JWTs
* verify JWTs
* extract claims
* extract subject/username
* check expiration

The signing key depends on the JWT algorithm.

```text
HMAC
  └── Secret Key

RSA
  └── Private Key

ECDSA
  └── Private Key
```

---

# `JwtAuthenticationFilter.java`

Runs for incoming requests and looks for:

```http
Authorization: Bearer <JWT>
```

Its responsibilities include:

1. Read Authorization header.
2. Extract Bearer token.
3. Validate JWT.
4. Extract username/subject.
5. Load user if required.
6. Create `Authentication`.
7. Store authentication in `SecurityContext`.

---

# `application.properties`

Contains application configuration.

Examples include:

```properties
spring.datasource.url=...
spring.datasource.username=...
spring.datasource.password=...

spring.jpa.hibernate.ddl-auto=update

jwt.expiration=3600000
```

For HMAC:

```properties
jwt.secret=...
```

may be used.

For RSA/ECDSA, the JWT signing secret is replaced by private/public key configuration.

### Important

Real secrets and private keys should not be committed to Git.

Use:

* environment variables
* secret managers
* mounted secrets
* deployment platform secret configuration

for real applications.

---

# `private_key.pem`

Used by asymmetric JWT algorithms:

```text
RSA
ECDSA
```

Its responsibility is:

```text
SIGN JWT
```

It must remain private.

---

# `public_key.pem`

Used by asymmetric JWT algorithms:

```text
RSA
ECDSA
```

Its responsibility is:

```text
VERIFY JWT
```

It does not allow someone to create a valid signature.

---

# 🔄 Registration Flow

When a user registers:

```text
Client
  │
  │ username + password
  ▼
AuthController
  │
  ▼
AuthService
  │
  ▼
PasswordEncoder
  │
  ▼
Argon2 / SCrypt / BCrypt
  │
  ▼
Password Hash
  │
  ▼
UserEntity
  │
  ▼
UserRepository
  │
  ▼
PostgreSQL
```

For the current version of the project:

```text
Password
   │
   ▼
Argon2PasswordEncoder
   │
   ▼
Argon2 Hash
   │
   ▼
Database
```

---

# 🔑 Login Flow

When a user logs in:

```text
Client
  │
  │ username + password
  ▼
AuthController
  │
  ▼
AuthenticationManager
  │
  ▼
DaoAuthenticationProvider
  │
  ▼
CustomUserDetailsService
  │
  ▼
UserRepository
  │
  ▼
Database
  │
  ▼
UserDetails
  │
  ▼
PasswordEncoder
  │
  ▼
Argon2
  │
  ▼
Password matches?
  │
  ├── NO  → Authentication Failure
  │
  └── YES
       │
       ▼
 Authentication Success
       │
       ▼
   JwtService
       │
       ▼
   JWT Signing
       │
       ▼
      JWT
```

---

# ⚙️ AuthenticationManager

`AuthenticationManager` is the main entry point for authentication.

When we call:

```java
authenticationManager.authenticate(
    authentication
);
```

we are effectively saying:

> Spring Security, authenticate these credentials.

Conceptually:

```text
AuthenticationManager
        │
        ▼
AuthenticationProvider
        │
        ▼
Authentication Result
```

---

# 🔧 DaoAuthenticationProvider

`DaoAuthenticationProvider` performs username/password authentication using:

```text
UserDetailsService
+
PasswordEncoder
```

Flow:

```text
DaoAuthenticationProvider
        │
        ├───────────────┐
        ▼               ▼
UserDetailsService   PasswordEncoder
        │               │
        ▼               ▼
   UserDetails      Verify Password
        │               │
        └───────┬───────┘
                ▼
        Authentication
```

It does not generate JWT.

JWT generation happens after successful authentication.

---

# 🔎 CustomUserDetailsService

Spring Security indirectly calls:

```java
loadUserByUsername(username)
```

during username/password authentication.

Flow:

```text
AuthenticationManager
        │
        ▼
DaoAuthenticationProvider
        │
        ▼
CustomUserDetailsService
        │
        ▼
UserRepository
        │
        ▼
Database
        │
        ▼
UserEntity
        │
        ▼
UserDetails
```

This is why `CustomUserDetailsService` is important even though the controller may never directly call it.

---

# 🎟️ JWT

JWT stands for:

> JSON Web Token

A JWT is commonly used to carry claims between a client and server.

Example:

```text
eyJhbGciOiJFUzI1NiJ9
.
eyJzdWIiOiJyYWphdCJ9
.
SIGNATURE
```

A JWT consists of three parts:

```text
HEADER.PAYLOAD.SIGNATURE
```

---

# 🧩 JWT Structure

## Header

Contains metadata such as the signing algorithm.

Example:

```json
{
    "alg": "ES256"
}
```

---

## Payload

Contains claims.

Example:

```json
{
    "sub": "rajat",
    "iat": 1790106334,
    "exp": 1790109934
}
```

The payload is **not encrypted simply because it is a JWT**.

Anyone who obtains the token can generally decode its header and payload.

Therefore:

> Do not put sensitive secrets or passwords inside JWT claims.

---

## Signature

The signature protects the integrity/authenticity of the signed JWT.

Conceptually:

```text
Header
+
Payload
+
Signing Key
      │
      ▼
Signature
```

---

# 🔐 JWT Signing Algorithms

JWT commonly uses three important families:

```text
HMAC
RSA
ECDSA
```

They can be divided into:

```text
                 JWT SIGNING
                     │
          ┌──────────┴──────────┐
          │                     │
          ▼                     ▼
      Symmetric             Asymmetric
          │                     │
          ▼                ┌────┴────┐
        HMAC               RSA     ECDSA
```

---

# 🔵 Symmetric Cryptography

Symmetric cryptography uses the **same secret key** for both operations.

```text
          SAME SECRET
          ┌─────────┐
          │         │
          ▼         ▼
       SIGN       VERIFY
```

For JWT, the major symmetric algorithm family is:

```text
HMAC
```

Examples:

```text
HS256
HS384
HS512
```

---

# 🔵 HMAC

HMAC stands for:

> Hash-based Message Authentication Code

In JWT:

```text
HS256
```

means HMAC using SHA-256.

---

## HMAC Architecture

```text
                SHARED SECRET
                     │
            ┌────────┴────────┐
            │                 │
            ▼                 ▼
         SERVER A          SERVER B
            │                 │
          SIGN              VERIFY
            │                 │
            └────────┬────────┘
                     │
                     ▼
                    JWT
```

The same secret is needed to create and verify the signature.

---

## HMAC JWT Flow

Signing:

```text
Header
  +
Payload
  +
Secret
  │
  ▼
HMAC
  │
  ▼
Signature
```

Verification:

```text
JWT
 │
 ▼
Header + Payload
 │
 ▼
Same Secret
 │
 ▼
Calculate Signature
 │
 ▼
Compare
```

---

# ⚠️ HMAC Key Management

The biggest conceptual difference is:

```text
HMAC
 ↓
One shared secret
```

Every service that needs to verify or create tokens must possess that secret.

Therefore:

```text
Service A
   │
   └── SECRET

Service B
   │
   └── SAME SECRET
```

If many independent services need verification, distributing the same secret becomes a key-management concern.

---

# 🔴 Asymmetric Cryptography

Asymmetric cryptography uses a **key pair**:

```text
Private Key
Public Key
```

The keys are mathematically related, but they have different responsibilities.

For JWT signing:

```text
Private Key
     │
     ▼
   SIGN
     │
     ▼
    JWT
```

Verification:

```text
JWT
 │
 ▼
Public Key
 │
 ▼
VERIFY
```

The public key cannot be used as a replacement for the private signing key.

---

# 🔴 RSA

RSA is an asymmetric cryptographic algorithm.

For JWT:

```text
RS256
```

means:

```text
RSA
+
SHA-256
```

---

# 🔑 Generate RSA Private Key

Example:

```bash
openssl genpkey \
  -algorithm RSA \
  -pkeyopt rsa_keygen_bits:3072 \
  -out private_key.pem
```

This creates:

```text
private_key.pem
```

---

# 🔑 Generate RSA Public Key

```bash
openssl rsa \
  -pubout \
  -in private_key.pem \
  -out public_key.pem
```

Now:

```text
private_key.pem
       │
       │ derives
       ▼
public_key.pem
```

---

# 🔐 RSA JWT Architecture

```text
              RSA KEY PAIR
        ┌───────────────────────┐
        │                       │
        │   Private Key         │
        │   Public Key          │
        │                       │
        └───────────┬───────────┘
                    │
               JwtKeyConfig
                    │
          ┌─────────┴─────────┐
          ▼                   ▼
     PrivateKey           PublicKey
          │                   │
        SIGN                VERIFY
          │                   │
          ▼                   ▼
     JwtService      JwtAuthenticationFilter
          │                   │
          ▼                   │
         JWT ─────────────────┘
                              │
                              ▼
                       SecurityContext
```

---

# 🟢 ECDSA

ECDSA stands for:

> Elliptic Curve Digital Signature Algorithm

It is another asymmetric digital signature algorithm.

For JWT:

```text
ES256
```

means:

```text
ECDSA
+
SHA-256
+
P-256 curve
```

Therefore:

```text
ES256 = ECDSA + SHA-256 + P-256
```

---

# 🔑 Generate ECDSA Private Key

Using P-256:

```bash
openssl genpkey \
  -algorithm EC \
  -pkeyopt ec_paramgen_curve:P-256 \
  -out private_key.pem
```

---

# 🔑 Generate ECDSA Public Key

```bash
openssl ec \
  -in private_key.pem \
  -pubout \
  -out public_key.pem
```

Architecture:

```text
EC Private Key
      │
      │ derives
      ▼
EC Public Key
```

---

# 🧬 ECDSA JWT Architecture

```text
              EC KEY PAIR
        ┌───────────────────────┐
        │                       │
        │   Private Key         │
        │   Public Key          │
        │                       │
        └───────────┬───────────┘
                    │
               JwtKeyConfig
                    │
          ┌─────────┴─────────┐
          ▼                   ▼
     PrivateKey           PublicKey
          │                   │
        SIGN                VERIFY
          │                   │
          ▼                   ▼
     JwtService      JwtAuthenticationFilter
          │                   │
          ▼                   │
         JWT ─────────────────┘
                              │
                              ▼
                       SecurityContext
```

---

# 🆚 RSA vs ECDSA

| Feature          | RSA              | ECDSA            |
| ---------------- | ---------------- | ---------------- |
| JWT Algorithm    | RS256            | ES256            |
| Type             | Asymmetric       | Asymmetric       |
| Keys             | Private + Public | Private + Public |
| Signing          | Private key      | Private key      |
| Verification     | Public key       | Public key       |
| Hash             | SHA-256 in RS256 | SHA-256 in ES256 |
| Curve            | Not applicable   | P-256            |
| Typical key size | Larger           | Smaller          |
| Signature size   | Larger           | Smaller          |
| JWT support      | Very common      | Very common      |

The important conceptual similarity:

```text
RSA
Private → Sign
Public  → Verify


ECDSA
Private → Sign
Public  → Verify
```

---

# 🔥 HMAC vs RSA vs ECDSA

| Feature                   | HMAC          | RSA         | ECDSA       |
| ------------------------- | ------------- | ----------- | ----------- |
| JWT family                | Symmetric     | Asymmetric  | Asymmetric  |
| Example                   | HS256         | RS256       | ES256       |
| Signing key               | Shared secret | Private key | Private key |
| Verification key          | Same secret   | Public key  | Public key  |
| Same key for sign/verify? | Yes           | No          | No          |
| Private/public pair       | No            | Yes         | Yes         |
| Private key required      | No            | Yes         | Yes         |
| Public key required       | No            | Yes         | Yes         |
| Signature verification    | Shared secret | Public key  | Public key  |
| JWT `JwtService`          | HMAC          | RSA         | ECDSA       |

---

# 🧠 The Most Important Difference

Remember this:

## HMAC

```text
             SAME SECRET
             /        \
            ▼          ▼
         SIGN        VERIFY
```

## RSA

```text
       PRIVATE KEY          PUBLIC KEY
            │                   │
          SIGN                VERIFY
            │                   │
            └─────── JWT ──────┘
```

## ECDSA

```text
       PRIVATE KEY          PUBLIC KEY
            │                   │
          SIGN                VERIFY
            │                   │
            └─────── JWT ──────┘
```

---

# 🔐 Password Hashing + JWT Signing Together

These technologies solve different problems.

For example, our application can use:

```text
                    APPLICATION
                         │
             ┌───────────┴───────────┐
             │                       │
             ▼                       ▼
        PASSWORD SECURITY        JWT SECURITY
             │                       │
             ▼                       ▼
           Argon2                  ES256
             │                       │
             ▼                       ▼
       Password Hash            ECDSA Signature
             │                       │
             ▼                       ▼
         Database                  JWT
```

Another valid architecture could be:

```text
Argon2 + RS256
```

or:

```text
Argon2 + HS256
```

The two choices are independent.

---

# 🎟️ JwtService

`JwtService` is responsible for JWT creation and verification.

Conceptually:

```text
UserDetails
     │
     ▼
JwtService
     │
     ├── Header
     ├── Claims
     ├── Expiration
     └── Signature
```

For ECDSA:

```text
UserDetails
     │
     ▼
JwtService
     │
     ▼
EC Private Key
     │
     ▼
ES256
     │
     ▼
Signed JWT
```

For RSA:

```text
UserDetails
     │
     ▼
JwtService
     │
     ▼
RSA Private Key
     │
     ▼
RS256
     │
     ▼
Signed JWT
```

For HMAC:

```text
UserDetails
     │
     ▼
JwtService
     │
     ▼
Shared Secret
     │
     ▼
HS256
     │
     ▼
Signed JWT
```

---

# 🛡️ JwtAuthenticationFilter

After login, the client sends:

```http
Authorization: Bearer <JWT>
```

The filter reads this token.

Flow:

```text
Client
  │
  │ Authorization: Bearer <JWT>
  ▼
Security Filter Chain
  │
  ▼
JwtAuthenticationFilter
  │
  ▼
JwtService
  │
  ▼
Verify JWT
```

Depending on the algorithm:

```text
HS256 → Shared Secret
RS256 → RSA Public Key
ES256 → EC Public Key
```

---

# 🧠 SecurityContext

Once the JWT has been successfully validated:

```text
JWT
 │
 ▼
JwtAuthenticationFilter
 │
 ▼
Authentication
 │
 ▼
SecurityContext
```

The `SecurityContext` contains the authentication information for the current request.

Conceptually:

```text
SecurityContext
      │
      ▼
Authentication
      │
      ├── Principal
      ├── Authorities
      └── Authenticated
```

The controller can then access the authenticated user.

---

# 🔄 Complete Login Architecture

## Stage 1 — Password Authentication

```text
Username + Password
        │
        ▼
AuthenticationManager
        │
        ▼
DaoAuthenticationProvider
        │
        ▼
CustomUserDetailsService
        │
        ▼
UserRepository
        │
        ▼
Database
        │
        ▼
UserDetails
        │
        ▼
PasswordEncoder
        │
        ▼
Argon2
        │
        ▼
Authentication SUCCESS
```

---

# Stage 2 — JWT Creation

```text
Authentication SUCCESS
        │
        ▼
UserDetails
        │
        ▼
JwtService
        │
        ▼
JWT Signing Algorithm
        │
        ├── HS256 → Secret
        │
        ├── RS256 → RSA Private Key
        │
        └── ES256 → EC Private Key
        │
        ▼
Signed JWT
        │
        ▼
Client
```

---

# 🔄 Complete JWT Request Architecture

After login:

```text
Client
   │
   │ Authorization: Bearer <JWT>
   ▼
Spring Security Filter Chain
   │
   ▼
JwtAuthenticationFilter
   │
   ▼
JwtService
   │
   ▼
Signature Verification
   │
   ├── HS256 → Shared Secret
   │
   ├── RS256 → RSA Public Key
   │
   └── ES256 → EC Public Key
   │
   ▼
Signature Valid?
   │
   ├── NO
   │    │
   │    ▼
   │   401 Unauthorized
   │
   └── YES
        │
        ▼
   Authentication
        │
        ▼
   SecurityContext
        │
        ▼
    Controller
```

---

# 🧠 Stateless Authentication

JWT authentication is commonly implemented as stateless authentication.

The server does not need to maintain a traditional login session for every client.

Instead:

```text
Client
   │
   │ JWT
   ▼
Server
```

Each request carries the information required to authenticate the request.

However, stateless JWT authentication does **not** automatically solve:

* token revocation
* stolen-token handling
* refresh-token management
* logout invalidation
* token rotation

Those are separate architectural concerns.

---

# 🚨 Important Security Rules

## 1. Never store plain-text passwords

Bad:

```text
password = "ourpassword"
```

Good:

```text
password = Argon2 hash
```

---

## 2. Never put passwords inside JWTs

Never create claims such as:

```json
{
    "username": "rajat",
    "password": "ourpassword"
}
```

JWT claims should contain only the information actually required by the application.

---

## 3. Never expose asymmetric private keys

For RSA:

```text
RSA Private Key
```

For ECDSA:

```text
EC Private Key
```

must remain private.

```text
Backend
  │
  └── private_key.pem
```

Do not send it to the frontend.

---

## 4. HMAC secret must also remain secret

HMAC does not have a public key.

Instead:

```text
Shared Secret
```

must remain secret.

Anyone possessing the HMAC secret can potentially create valid signatures.

---

## 5. JWT payload is not automatically encrypted

A signed JWT protects integrity/authenticity.

It does not mean:

```text
Payload = encrypted
```

Do not put confidential information into a normal signed JWT simply because it is encoded.

---

# 🧩 Complete Component Reference

| Component                   | Responsibility                           |
| --------------------------- | ---------------------------------------- |
| `SecurityConfig`            | Configures Spring Security               |
| `JwtKeyConfig`              | Loads/configures cryptographic keys      |
| `AuthController`            | Handles authentication HTTP endpoints    |
| `AuthService`               | Defines authentication operations        |
| `AuthServiceImpl`           | Implements authentication operations     |
| `LoginRequestDTO`           | Carries login request data               |
| `LoginResponseDTO`          | Carries login response data              |
| `UserEntity`                | Represents database user                 |
| `UserRepository`            | Communicates with database               |
| `CustomUserDetailsService`  | Loads users for Spring Security          |
| `UserDetails`               | Spring Security representation of a user |
| `DaoAuthenticationProvider` | Authenticates username/password          |
| `AuthenticationManager`     | Coordinates authentication               |
| `PasswordEncoder`           | Password hashing/verifying abstraction   |
| `BCryptPasswordEncoder`     | BCrypt password hashing                  |
| `SCryptPasswordEncoder`     | SCrypt password hashing                  |
| `Argon2PasswordEncoder`     | Argon2 password hashing                  |
| `JwtService`                | Creates/verifies JWTs                    |
| `JwtAuthenticationFilter`   | Processes JWTs on requests               |
| `SecurityContext`           | Stores current request authentication    |
| `private_key.pem`           | Signs asymmetric JWTs                    |
| `public_key.pem`            | Verifies asymmetric JWTs                 |
| HMAC secret                 | Signs/verifies HMAC JWTs                 |

---

# 🧠 Three Layers of Security

It is useful to think about the application as three separate layers.

## Layer 1 — Password Security

```text
Password
   │
   ▼
Argon2
   │
   ▼
Password Hash
   │
   ▼
Database
```

---

## Layer 2 — Authentication

```text
Username + Password
       │
       ▼
AuthenticationManager
       │
       ▼
DaoAuthenticationProvider
       │
       ▼
UserDetailsService
       │
       ▼
Database
       │
       ▼
Authentication
```

---

## Layer 3 — Token Security

```text
Authentication
      │
      ▼
JwtService
      │
      ▼
HMAC / RSA / ECDSA
      │
      ▼
JWT
```

This separation makes Spring Security much easier to understand.

---

# 📊 Complete Security Technology Map

```text
                         SPRING SECURITY
                               │
          ┌────────────────────┼────────────────────┐
          │                    │                    │
          ▼                    ▼                    ▼
     Passwords            Authentication           JWT
          │                    │                    │
          ▼                    ▼                    ▼
   PasswordEncoder      AuthenticationManager   JwtService
          │                    │                    │
     ┌────┼────┐              ▼              ┌─────┼─────┐
     │    │    │       DaoAuthentication      │     │     │
     ▼    ▼    ▼          Provider            ▼     ▼     ▼
 BCrypt SCrypt Argon2          │            HMAC   RSA  ECDSA
                               │
                               ▼
                    CustomUserDetailsService
                               │
                               ▼
                         UserRepository
                               │
                               ▼
                           Database
```

---

# 🧠 Most Important Concept

There are two different cryptographic purposes in this project.

## Password Hashing

```text
Password
    │
    ▼
BCrypt / SCrypt / Argon2
    │
    ▼
Hash
    │
    ▼
Database
```

This protects stored passwords.

---

## JWT Signing

```text
JWT
 │
 ▼
HMAC / RSA / ECDSA
 │
 ▼
Signature
```

This protects the integrity/authenticity of the token.

---

# 🔥 Final Comparison

| Technology | Category                | Purpose                  | Key Type                   |
| ---------- | ----------------------- | ------------------------ | -------------------------- |
| BCrypt     | Password hashing        | Store/verify passwords   | Salt + internal parameters |
| SCrypt     | Password hashing        | Store/verify passwords   | Salt + parameters          |
| Argon2     | Password hashing        | Store/verify passwords   | Salt + parameters          |
| HMAC       | Symmetric cryptography  | JWT signing/verification | Shared secret              |
| RSA        | Asymmetric cryptography | JWT signing/verification | Private + Public           |
| ECDSA      | Asymmetric cryptography | JWT signing/verification | Private + Public           |

The most important distinction:

```text
             PASSWORD SECURITY
                    │
        ┌───────────┼───────────┐
        ▼           ▼           ▼
      BCrypt      SCrypt      Argon2


               JWT SECURITY
                    │
        ┌───────────┼───────────┐
        ▼           ▼           ▼
       HMAC        RSA         ECDSA
     Symmetric   Asymmetric   Asymmetric
```

---

# 🚀 Learning Order

For understanding this project properly:

```text
1. UserEntity
       ↓
2. UserRepository
       ↓
3. UserDetails
       ↓
4. UserDetailsService
       ↓
5. CustomUserDetailsService
       ↓
6. PasswordEncoder
       ↓
7. BCrypt
       ↓
8. SCrypt
       ↓
9. Argon2
       ↓
10. DaoAuthenticationProvider
       ↓
11. AuthenticationManager
       ↓
12. SecurityConfig
       ↓
13. Authentication
       ↓
14. JWT
       ↓
15. HMAC
       ↓
16. RSA
       ↓
17. ECDSA
       ↓
18. JwtService
       ↓
19. JwtAuthenticationFilter
       ↓
20. SecurityContext
       ↓
21. Stateless Authentication
```

---

# 🧠 Final Mental Model

If you remember only one diagram from this project, remember this:

```text
                         USER
                          │
                          ▼
                      REGISTER
                          │
                          ▼
                    PasswordEncoder
                          │
              ┌───────────┼───────────┐
              ▼           ▼           ▼
           BCrypt       SCrypt      Argon2
              │           │           │
              └───────────┼───────────┘
                          │
                          ▼
                      Password Hash
                          │
                          ▼
                       DATABASE
                          │
                          │
                          ▼
                         LOGIN
                          │
                          ▼
                 AuthenticationManager
                          │
                          ▼
                DaoAuthenticationProvider
                          │
                          ▼
                CustomUserDetailsService
                          │
                          ▼
                    UserRepository
                          │
                          ▼
                       Database
                          │
                          ▼
                      UserDetails
                          │
                          ▼
                   PasswordEncoder
                          │
                          ▼
                   Password Verification
                          │
                    ┌─────┴─────┐
                    │           │
                   NO          YES
                    │           │
                    ▼           ▼
                  401      Authentication
                                │
                                ▼
                            JwtService
                                │
                ┌───────────────┼───────────────┐
                │               │               │
                ▼               ▼               ▼
              HMAC             RSA            ECDSA
                │               │               │
          Shared Secret     Private Key     Private Key
                │               │               │
                └───────────────┼───────────────┘
                                │
                                ▼
                              JWT
                                │
                                ▼
                              CLIENT
                                │
                                │
                     Authorization: Bearer JWT
                                │
                                ▼
                    JwtAuthenticationFilter
                                │
                                ▼
                         JwtService
                                │
                ┌───────────────┼───────────────┐
                │               │               │
                ▼               ▼               ▼
          Shared Secret     Public Key      Public Key
             (HMAC)           (RSA)          (ECDSA)
                │               │               │
                └───────────────┼───────────────┘
                                │
                                ▼
                       Signature Valid?
                          /           \
                        NO             YES
                        │               │
                        ▼               ▼
                  401 Unauthorized   Authentication
                                        │
                                        ▼
                                  SecurityContext
                                        │
                                        ▼
                                    Controller
```

---

# 📌 Final Summary

### `UserDetailsService`

Loads a user's security information from the application's data source.

### `UserDetails`

Represents that user in a format Spring Security understands.

### `DaoAuthenticationProvider`

Uses `UserDetailsService` and `PasswordEncoder` to authenticate username/password credentials.

### `AuthenticationManager`

Coordinates the authentication process.

### `PasswordEncoder`

Provides the abstraction for password hashing and verification.

### `BCryptPasswordEncoder`

Uses BCrypt for password hashing.

### `SCryptPasswordEncoder`

Uses SCrypt for password hashing with memory-hard characteristics.

### `Argon2PasswordEncoder`

Uses Argon2 for modern memory-hard password hashing.

### `JwtService`

Creates and verifies JWTs.

### `HMAC`

A **symmetric** JWT signing mechanism using the same secret for signing and verification.

### `RSA`

An **asymmetric** JWT signing mechanism using a private key for signing and a public key for verification.

### `ECDSA`

An **asymmetric** JWT signing mechanism using an EC private key for signing and an EC public key for verification.

### `JwtAuthenticationFilter`

Reads and validates JWTs from incoming requests.

### `SecurityContext`

Stores authentication information for the current request.

### Private Key

Signs asymmetric JWTs.

### Public Key

Verifies asymmetric JWT signatures.

---

# 🛠️ Project Purpose

This repository is intentionally built as a **Spring Security learning project**.

The goal is to understand the complete authentication pipeline:

```text
Database
   ↓
UserDetailsService
   ↓
DaoAuthenticationProvider
   ↓
AuthenticationManager
   ↓
Authentication
   ↓
JwtService
   ↓
JWT
   ↓
JwtAuthenticationFilter
   ↓
Signature Verification
   ↓
SecurityContext
   ↓
Protected Controller
```

And, at the cryptographic level:

```text
PASSWORD SECURITY
        │
        ├── BCrypt
        ├── SCrypt
        └── Argon2


JWT SECURITY
        │
        ├── HMAC
        ├── RSA
        └── ECDSA
```

The most important thing is to understand **why each component exists, what problem it solves, and who calls it**, rather than memorizing Spring Security configuration from tutorials.
