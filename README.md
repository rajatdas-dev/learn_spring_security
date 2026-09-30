# 🔐 Learn Spring Security

> A practical Spring Security project created **just for learning and understanding how Spring Security works internally**.

This project focuses on understanding the complete authentication and authorization pipeline instead of simply copying Spring Security configuration from tutorials.

The project currently covers:

* Password hashing
* Username/password authentication
* `UserDetailsService`
* `AuthenticationManager`
* `DaoAuthenticationProvider`
* JWT authentication
* HMAC, RSA and ECDSA
* JWT filters
* `SecurityContext`
* Stateless authentication
* Role-Based Access Control (RBAC)
* Method-level authorization
* Custom `401 Unauthorized` handling
* Custom `403 Forbidden` handling

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
10. [Argon2 and Bouncy Castle Dependency](#-argon2-and-bouncy-castle-dependency)
11. [Password Encoder Comparison](#-password-encoder-comparison)
12. [Project Architecture](#-project-architecture)
13. [File-by-File Explanation](#-file-by-file-explanation)
14. [Registration Flow](#-registration-flow)
15. [Login Flow](#-login-flow)
16. [AuthenticationManager](#-authenticationmanager)
17. [DaoAuthenticationProvider](#-daoauthenticationprovider)
18. [CustomUserDetailsService](#-customuserdetailsservice)
19. [JWT](#-jwt)
20. [JWT Structure](#-jwt-structure)
21. [JWT Signing Algorithms](#-jwt-signing-algorithms)
22. [Symmetric Cryptography — HMAC](#-symmetric-cryptography--hmac)
23. [Asymmetric Cryptography](#-asymmetric-cryptography)
24. [RSA](#-rsa)
25. [ECDSA](#-ecdsa)
26. [RSA vs ECDSA](#-rsa-vs-ecdsa)
27. [HMAC vs RSA vs ECDSA](#-hmac-vs-rsa-vs-ecdsa)
28. [JwtService](#-jwtservice)
29. [JwtAuthenticationFilter](#-jwtauthenticationfilter)
30. [SecurityContext](#-securitycontext)
31. [Role-Based Access Control — RBAC](#-role-based-access-control--rbac)
32. [Roles and Authorities](#-roles-and-authorities)
33. [Method-Level Security](#-method-level-security)
34. [Role Assignment](#-role-assignment)
35. [Hybrid RBAC + ABAC](#-hybrid-rbac--abac)
36. [ABAC for User Management](#-abac-for-user-management)
37. [ABAC for Documents](#-abac-for-documents)
38. [RSA and ECDSA Key Generation](#-rsa-and-ecdsa-key-generation)
39. [Custom 401 and 403 Exception Handling](#-custom-401-and-403-exception-handling)
40. [Complete Login Architecture](#-complete-login-architecture)
41. [Complete JWT Request Architecture](#-complete-jwt-request-architecture)
42. [Complete RBAC Request Architecture](#-complete-rbac-request-architecture)
43. [Stateless Authentication](#-stateless-authentication)
44. [Important Security Rules](#-important-security-rules)
45. [Complete Component Reference](#-complete-component-reference)
46. [Learning Order](#-learning-order)
47. [Final Mental Model](#-final-mental-model)

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
11. Gets their roles/authorities
12. Gets authorization checked
13. Reaches a protected controller

The complete security architecture can be divided into:

```text
                    APPLICATION SECURITY
                           │
          ┌────────────────┼────────────────┐
          │                │                │
          ▼                ▼                ▼
     Password          Authentication   Authorization
      Security              │                │
          │                 │                │
          ▼                 ▼                ▼
   PasswordEncoder      JWT / Auth       RBAC
          │                 │                │
          ▼                 ▼                ▼
    Password Hash       JWT Token       Roles
                                            │
                                            ▼
                                      Permissions
```

---

# 🧠 Big Picture

A very important concept in this project is that **password security, authentication, JWT signing, and authorization are different concerns**.

### Password security

```text
Password
    │
    ▼
PasswordEncoder
    │
    ▼
Password Hash
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

### Authentication

```text
Username + Password
        │
        ▼
AuthenticationManager
        │
        ▼
Authenticated User
```

### JWT security

```text
JWT Data
 │
 ▼
Cryptographic Signing
 │
 ├── HMAC
 ├── RSA
 └── ECDSA
```

### Authorization

```text
Authenticated User
        │
        ▼
Role / Authority
        │
        ▼
Access Decision
        │
        ├── Allowed
        │
        └── Forbidden
```

Therefore:

```text
PasswordEncoder
      ≠
JWT Signing
      ≠
Authorization
```

For example:

```text
Argon2 + RSA + RBAC
```

means:

```text
Argon2
  ↓
Password hashing

RSA
  ↓
JWT signing

RBAC
  ↓
Authorization
```

These are separate security mechanisms.

---

# 🔐 Hybrid RBAC + ABAC

The project uses a **hybrid authorization model**. RBAC provides the coarse-grained authorization boundary, while ABAC evaluates the context of a specific operation using the **subject**, **resource**, **action**, and optional **environment** attributes.

```text
Authenticated Request
        │
        ▼
JWT verified
        │
        ▼
SecurityContext
        │
        ▼
RBAC check
(role / authority)
        │
        ├── Denied ───────────────► 403
        │
        ▼
ABAC policy
(subject + resource + action + environment)
        │
        ├── Denied ───────────────► 403
        │
        ▼
Controller / Service
```

### RBAC vs ABAC

RBAC answers whether a role is allowed to attempt an operation. ABAC answers whether this particular subject is allowed to perform the operation against this particular resource under the current policy.

```text
RBAC
 ├── USER
 ├── MODERATOR
 └── ADMIN

ABAC
 ├── subject attributes   → username, role, department, ownership
 ├── resource attributes  → owner, department, classification
 ├── action               → READ, UPDATE, DELETE
 └── environment          → optional context such as time
```

A role is therefore not the only authorization attribute. For example, a `USER` can be allowed to read their own document while being denied access to another user's document.

---

# 👥 ABAC for User Management

User management remains protected by RBAC, with ABAC providing a place for target-resource and policy checks.

The role-management endpoint is:

```http
PATCH /admin/users/update/role
```

Its baseline boundary is:

```java
@PreAuthorize("hasRole('ADMIN')")
```

The caller's role must come from the authenticated server-side identity. The target user must be loaded from the database. Client input must not be able to grant the caller or another user an authority merely by submitting a role value.

Conceptually:

```text
Subject: authenticated caller
Resource: target UserEntity
Action: UPDATE_ROLE

Policy:
ADMIN + permitted target + permitted role transition
                     │
                     ▼
                  ALLOW / DENY
```

Future attribute rules can include protected/system accounts, administrative scope, permitted role transitions, and audit information describing who changed whose role.

Public registration must continue to assign `USER` server-side. A public request must never be trusted to choose `ADMIN` or `MODERATOR`.

---

# 📄 ABAC for Documents

Documents are the main resource used to demonstrate object-level ABAC. A document can contain attributes such as:

```text
Document
├── id
├── title
├── content
├── ownerUsername
├── department
├── classification
└── createdAt
```

The policy evaluates:

```text
Subject      → username / role / department
Resource     → owner / department / classification
Action       → READ / UPDATE / DELETE
Environment  → optional request context
```

A representative policy is:

```text
READ
 ├── ADMIN                 → allowed
 ├── MODERATOR             → allowed according to policy
 ├── OWNER                 → allowed
 └── otherwise             → denied

UPDATE
 ├── ADMIN                 → allowed
 ├── OWNER                 → allowed
 └── otherwise             → denied

DELETE
 ├── ADMIN                 → allowed
 ├── OWNER                 → allowed according to policy
 └── otherwise             → denied
```

A policy component can be called from method security:

```java
@PreAuthorize("@documentAccessPolicy.canRead(authentication, #id)")
```

The policy should load the target resource from the database and make its decision using server-side attributes. Missing resources or missing attributes should fail closed rather than being treated as allowed.

### Never trust ownership from the request body

When creating a document, derive ownership from the authenticated principal:

```java
String username = authentication.getName();
document.setOwnerUsername(username);
```

Do not use a client-supplied `ownerUsername` as the security authority for ownership. Otherwise, a caller could attempt to create a resource owned by someone else and undermine the ABAC policy.

### Hybrid decision

```text
JWT
 │
 ▼
Authentication
 │
 ▼
RBAC
 │
 │  Is the role allowed to attempt this operation?
 │
 ▼
ABAC
 │
 │  Does this subject have access to this exact resource?
 │
 ▼
Controller / Service
```

This is the main difference between endpoint-level RBAC and object-level ABAC.

---

# 🔑 RSA and ECDSA Key Generation

This project uses asymmetric cryptography for JWT signing. The **private key signs** the JWT and the **public key verifies** the signature.

> Run these commands from a terminal where OpenSSL is installed. Keep private keys out of source control and never commit production private keys to Git.

## RSA key pair

🔑 Generate RSA Private Key

Example:

```
openssl genpkey \
-algorithm RSA \
-pkeyopt rsa_keygen_bits:3072 \
-out private_key.pem

```

This creates:

private_key.pem

🔑 Generate RSA Public Key

```aiignore
openssl rsa \
-pubout \
-in private_key.pem \
-out public_key.pem
```

Now:

private_key.pem
│
│ derives
▼
public_key.pem

Inspect the private key:

```bash
openssl pkey -in rsa_private_key.pem -text -noout
```

Inspect the public key:

```bash
openssl pkey -pubin -in rsa_public_key.pem -text -noout
```

Result:

```text
rsa_private_key.pem  → signing
rsa_public_key.pem   → verification
```

For the current Java configuration, the selected pair can be copied/renamed to:

```text
src/main/resources/keys/private_key.pem
src/main/resources/keys/public_key.pem
```

Use:

```java
KeyFactory.getInstance("RSA");
```

and:

```java
.signWith(privateKey, Jwts.SIG.RS256)
```

---

## ECDSA / EC key pair

The current JWT configuration uses **ES256**, which uses the NIST P-256 curve.

Generate the EC private key:

```bash
openssl genpkey \
  -algorithm EC \
  -pkeyopt ec_paramgen_curve:P-256 \
  -out private_key.pem
```

Generate the corresponding public key:

```bash
openssl ec \
  -in private_key.pem \
  -pubout \
  -out public_key.pem
```

Inspect the private key:

```bash
openssl ec -in ecdsa_private_key.pem -text -noout
```

Inspect the public key:

```bash
openssl ec -pubin -in ecdsa_public_key.pem -text -noout
```

Result:

```text
ecdsa_private_key.pem  → signing
ecdsa_public_key.pem   → verification
```

For the current Java configuration, the selected pair can be copied/renamed to:

```text
src/main/resources/keys/private_key.pem
src/main/resources/keys/public_key.pem
```

Use:

```java
KeyFactory.getInstance("EC");
```

and:

```java
.signWith(privateKey, Jwts.SIG.ES256)
```

### Verify the ECDSA public/private pair

Derive a public key from the private key:

```bash
openssl ec -in ecdsa_private_key.pem -pubout -out derived_public_key.pem
```

Compare the files on Linux/macOS:

```bash
cmp ecdsa_public_key.pem derived_public_key.pem
```

On Windows PowerShell:

```powershell
fc.exe ecdsa_public_key.pem derived_public_key.pem
```

---

## RSA vs ECDSA command summary

| Algorithm | Private key command                                                                 | Public key command                                                    | JWT algorithm | Java `KeyFactory` |
|---|-------------------------------------------------------------------------------------|-----------------------------------------------------------------------|---|---|
| RSA | `openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:3072` | `openssl rsa -pubout -in private_key.pem -pubout -out public_key.pem` | `RS256` | `RSA` |
| ECDSA | `openssl genpkey -algorithm EC -pkeyopt ec_paramgen_curve:P-256 -out private_key.pem`        | `openssl ec -in private_key.pem -pubout -out public_key.pem`          | `ES256` | `EC` |

Do not mix algorithms or key pairs:

```text
RSA key pair
    ↓
KeyFactory("RSA")
    ↓
RS256

ECDSA key pair
    ↓
KeyFactory("EC")
    ↓
ES256
```

The private and public keys must belong to the same key pair.

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
AuthenticationManager
        │
        ▼
User authenticated
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
ROLE_USER
        │
        ▼
GET /user/profile
        │
        ▼
Allowed
```

But:

```text
ROLE_USER
   │
   ▼
DELETE /user/comments/10
   │
   ▼
Requires MODERATOR or ADMIN
   │
   ▼
403 Forbidden
```

Therefore:

```text
Authentication = Who are you?
Authorization  = What are you allowed to access?
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
Authentication
 │
 ▼
SecurityContext
 │
 ▼
Authorization
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

The application database can contain:

```text
UserEntity
├── id
├── username
├── password
└── role
```

Spring Security then converts this information into:

```text
UserDetails
├── username
├── password
└── authorities
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

This project explores:

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

It can be useful for:

* basic demonstrations
* temporary learning
* simple testing

It should **not** be used for real password storage.

---

# 2. BCrypt

BCrypt is a password hashing algorithm designed to make password guessing more expensive.

Example:

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

SCrypt is designed to be **memory-hard**.

It intentionally requires memory resources in addition to computational work.

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

Argon2 is memory-hard.

Important Argon2 variants include:

```text
Argon2d
Argon2i
Argon2id
```

Argon2id is commonly used for password hashing because it combines resistance characteristics associated with both Argon2i and Argon2d.

---

# 📦 Argon2 and Bouncy Castle Dependency

While using:

```java
Argon2PasswordEncoder
```

this project encountered a runtime error similar to:

```text
java.lang.NoClassDefFoundError:
org/bouncycastle/crypto/params/Argon2Parameters$Builder
```

The important part of the exception was:

```text
ClassNotFoundException:
org.bouncycastle.crypto.params.Argon2Parameters$Builder
```

This happened because Spring Security's Argon2 password encoder relies on **Bouncy Castle cryptographic classes**.

Therefore, the application needs the Bouncy Castle provider dependency.

The dependency added to the project is:

```xml
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk18on</artifactId>
    <version>1.86</version>
</dependency>
```

The dependency provides cryptographic classes required by the Argon2 implementation.

Conceptually:

```text
Argon2PasswordEncoder
        │
        ▼
Spring Security
        │
        ▼
Bouncy Castle
        │
        ▼
Argon2 cryptographic implementation
```

### Why did BCrypt work without it?

BCrypt does not require this additional Bouncy Castle dependency in the same way.

Therefore:

```text
BCrypt
   │
   └── Spring Security

SCrypt
   │
   └── Spring Security

Argon2
   │
   └── Spring Security
            │
            ▼
      Bouncy Castle
```

The important lesson is:

> Adding the Bouncy Castle dependency does not change how Argon2 works. It provides the cryptographic classes required by the Argon2 implementation used by the application.

---

# 🔄 BCrypt vs SCrypt vs Argon2

| Feature                             | BCrypt           | SCrypt           | Argon2                      |
| ----------------------------------- | ---------------- | ---------------- | --------------------------- |
| Purpose                             | Password hashing | Password hashing | Password hashing            |
| One-way                             | Yes              | Yes              | Yes                         |
| Salted                              | Yes              | Yes              | Yes                         |
| Memory-hard                         | No               | Yes              | Yes                         |
| Configurable work                   | Yes              | Yes              | Yes                         |
| Spring `PasswordEncoder`            | Yes              | Yes              | Yes                         |
| JWT generation                      | No               | No               | No                          |
| Encryption                          | No               | No               | No                          |
| Additional Bouncy Castle dependency | No               | No               | Yes, for this project setup |

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
    │               │   ├── AuthController.java
    │               │   └── UserController.java
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
    │               ├── response/
    │               │   └── ApiResponse.java
    │               │
    │               ├── service/
    │               │   ├── AuthService.java
    │               │   └── AuthServiceImpl.java
    │               │
    │               └── security/
    │                   ├── CustomUserDetailsService.java
    │                   ├── CustomAccessDeniedHandler.java
    │                   ├── CustomAuthenticationEntryPoint.java
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
* Enable method-level security
* Configure custom `401` handling
* Configure custom `403` handling

Important:

```java
@EnableMethodSecurity
```

enables annotations such as:

```java
@PreAuthorize(...)
```

---

# `JwtKeyConfig.java`

Responsible for loading cryptographic keys used by asymmetric JWT algorithms.

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

---

# `AuthController.java`

HTTP/API layer for authentication.

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

---

# `UserController.java`

Contains protected user-related endpoints and demonstrates RBAC.

Examples:

```http
GET /user/profile
PUT /user/profile
GET /user/dashboard
GET /user/moderation
DELETE /user/comments/{commentId}
GET /user/admin/reports
```

Authorization is implemented using:

```java
@PreAuthorize(...)
```

Example:

```java
@PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
```

---

# `UserEntity.java`

Represents the application user stored in the database.

Current conceptual structure:

```text
UserEntity
├── id
├── username
├── password
└── role
```

The password contains a **password hash**, not the original password.

The role identifies the user's authorization level.

Example:

```text
USER
MODERATOR
ADMIN
```

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
Argon2
  │
  ▼
UserEntity
  │
  ▼
UserRepository
  │
  ▼
Database
```

The normal registration flow assigns:

```text
USER
```

to a newly registered account.

The client should **not** be allowed to submit:

```json
{
    "username": "rajat",
    "password": "password",
    "role": "ADMIN"
}
```

and make itself an administrator.

---

# `CustomUserDetailsService.java`

Connects the application's database with Spring Security.

Flow:

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
   ├── username
   ├── password
   └── role
   │
   ▼
UserDetails
   │
   ▼
Authorities
```

The role stored in the database is converted into a Spring Security authority.

For example:

```text
Database role:

USER

Spring Security authority:

ROLE_USER
```

Similarly:

```text
MODERATOR → ROLE_MODERATOR

ADMIN → ROLE_ADMIN
```

---

# `CustomAccessDeniedHandler.java`

Handles authorization failures.

It is used when:

> The user is authenticated but does not have sufficient permissions.

Example:

```text
ROLE_USER
    │
    ▼
DELETE /user/comments/10
    │
    ▼
Requires ROLE_MODERATOR or ROLE_ADMIN
    │
    ▼
AccessDeniedException
    │
    ▼
CustomAccessDeniedHandler
    │
    ▼
403 Forbidden
```

The handler can also log useful information:

```text
403 FORBIDDEN
method=DELETE
uri=/user/comments/10
```

and return the application's standard `ApiResponse`.

---

# `CustomAuthenticationEntryPoint.java`

Handles authentication failures.

It is used when:

> The request requires authentication, but the user is not successfully authenticated.

Examples:

```text
No JWT
Invalid JWT
Expired JWT
Unauthenticated request
```

Flow:

```text
Request
   │
   ▼
Authentication required
   │
   ▼
Authentication missing/invalid
   │
   ▼
CustomAuthenticationEntryPoint
   │
   ▼
401 Unauthorized
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

Signing key:

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

Responsibilities:

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

Examples:

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

For RSA/ECDSA, private/public key configuration is used instead.

Real secrets should not be committed to Git.

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
Argon2
  │
  ▼
Password Hash
  │
  ▼
UserEntity
  │
  ├── username
  ├── password hash
  └── role = USER
  │
  ▼
UserRepository
  │
  ▼
PostgreSQL
```

Important security rule:

```text
Public Registration
        │
        ▼
Always USER
```

A normal registration request should not decide its own role.

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
  ├── NO  → Authentication Failure → 401
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

The role is also loaded from the database.

---

# 🎟️ JWT

JWT stands for:

> JSON Web Token

A JWT is commonly used to carry claims between a client and server.

Example:

```text
HEADER.PAYLOAD.SIGNATURE
```

---

# 🧩 JWT Structure

## Header

Example:

```json
{
    "alg": "ES256"
}
```

---

## Payload

Example:

```json
{
    "sub": "rajat",
    "iat": 1790106334,
    "exp": 1790109934
}
```

The payload is not automatically encrypted.

Do not put passwords or sensitive secrets into JWT claims.

---

## Signature

The signature protects the integrity/authenticity of the signed JWT.

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

JWT commonly uses:

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

# 🔵 Symmetric Cryptography — HMAC

Symmetric cryptography uses the same secret for signing and verification.

```text
          SAME SECRET
          ┌─────────┐
          │         │
          ▼         ▼
       SIGN       VERIFY
```

Examples:

```text
HS256
HS384
HS512
```

For example:

```text
HS256 = HMAC + SHA-256
```

---

# 🔴 Asymmetric Cryptography

Asymmetric cryptography uses:

```text
Private Key
Public Key
```

For JWT:

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

Architecture:

```text
RSA Private Key
       │
       ▼
      SIGN
       │
       ▼
      JWT
       │
       ▼
RSA Public Key
       │
       ▼
     VERIFY
```

---

# 🟢 ECDSA

ECDSA stands for:

> Elliptic Curve Digital Signature Algorithm

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

Architecture:

```text
EC Private Key
       │
       ▼
      SIGN
       │
       ▼
      JWT
       │
       ▼
EC Public Key
       │
       ▼
     VERIFY
```

---

# 🆚 RSA vs ECDSA

| Feature        | RSA              | ECDSA            |
| -------------- | ---------------- | ---------------- |
| JWT Algorithm  | RS256            | ES256            |
| Type           | Asymmetric       | Asymmetric       |
| Keys           | Private + Public | Private + Public |
| Signing        | Private key      | Private key      |
| Verification   | Public key       | Public key       |
| Hash           | SHA-256          | SHA-256          |
| Curve          | Not applicable   | P-256            |
| Signature size | Larger           | Smaller          |

---

# 🔥 HMAC vs RSA vs ECDSA

| Feature                   | HMAC          | RSA         | ECDSA       |
| ------------------------- | ------------- | ----------- | ----------- |
| Type                      | Symmetric     | Asymmetric  | Asymmetric  |
| Example                   | HS256         | RS256       | ES256       |
| Signing key               | Shared secret | Private key | Private key |
| Verification key          | Same secret   | Public key  | Public key  |
| Same key for sign/verify? | Yes           | No          | No          |
| Private/public pair       | No            | Yes         | Yes         |
| Private key required      | No            | Yes         | Yes         |
| Public key required       | No            | Yes         | Yes         |

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

For RBAC, authorities are especially important:

```text
ROLE_USER
ROLE_MODERATOR
ROLE_ADMIN
```

---

# 🛡️ Role-Based Access Control — RBAC

RBAC stands for:

> **Role-Based Access Control**

Instead of deciding access only based on whether a user is authenticated, the application also checks the user's role.

Example:

```text
User
 │
 ▼
Role
 │
 ├── USER
 ├── MODERATOR
 └── ADMIN
```

Then endpoints can require specific roles.

---

## Example

```java
@PreAuthorize("hasRole('ADMIN')")
```

means:

```text
Required authority:

ROLE_ADMIN
```

Another example:

```java
@PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
```

means:

```text
ROLE_MODERATOR OR ROLE_ADMIN
```

---

# 👥 Roles and Authorities

Suppose the database contains:

```text
role = USER
```

Spring Security can represent this as:

```text
ROLE_USER
```

The same mapping applies:

```text
Database Role       Spring Authority

USER          →     ROLE_USER

MODERATOR     →     ROLE_MODERATOR

ADMIN         →     ROLE_ADMIN
```

This distinction is important because:

```java
hasRole("ADMIN")
```

automatically works with:

```text
ROLE_ADMIN
```

Whereas:

```java
hasAuthority("ROLE_ADMIN")
```

expects the complete authority name.

Therefore:

```java
hasRole("ADMIN")
```

and:

```java
hasAuthority("ROLE_ADMIN")
```

are commonly equivalent.

Avoid:

```java
hasRole("ROLE_ADMIN")
```

because Spring's role prefix handling can result in an incorrect authority expectation.

---

# 🔒 Method-Level Security

This project uses:

```java
@EnableMethodSecurity
```

to enable annotations such as:

```java
@PreAuthorize(...)
```

Example:

```java
@GetMapping("/moderation")
@PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
public ResponseEntity<ApiResponse<?>> getModerationPanel() {
    ...
}
```

Without:

```java
@EnableMethodSecurity
```

the `@PreAuthorize` annotation will not enforce the role check.

---

# 🧩 Current RBAC Rules

The current user controller demonstrates:

```text
Endpoint                              USER   MODERATOR   ADMIN
----------------------------------------------------------------
GET    /user/profile                  ✅       ✅         ✅
PUT    /user/profile                  ✅       ✅         ✅
GET    /user/dashboard                ✅       ✅         ✅
GET    /user/moderation               ❌       ✅         ✅
DELETE /user/comments/{commentId}     ❌       ✅         ✅
GET    /user/admin/reports            ❌       ❌         ✅
```

Example:

```java
@PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
```

allows:

```text
ROLE_MODERATOR
ROLE_ADMIN
```

but denies:

```text
ROLE_USER
```

---

# 🚨 Authentication vs Authorization Errors

One of the most important RBAC concepts is the difference between:

```text
401 Unauthorized
```

and:

```text
403 Forbidden
```

---

## 401 Unauthorized

Means the request is not successfully authenticated.

Examples:

```text
No JWT
Invalid JWT
Expired JWT
Missing authentication
```

Spring Security uses:

```text
AuthenticationEntryPoint
```

The project uses:

```text
CustomAuthenticationEntryPoint
```

Flow:

```text
Request
   │
   ▼
Authentication required
   │
   ▼
Authentication failed/missing
   │
   ▼
CustomAuthenticationEntryPoint
   │
   ▼
401
```

Example response:

```json
{
    "success": false,
    "message": "Authentication required",
    "data": null
}
```

---

# 🚫 403 Forbidden

A `403 Forbidden` is different.

It means:

> The user is authenticated, but does not have sufficient authorization.

Example:

```text
USER
 │
 ▼
ROLE_USER
 │
 ▼
DELETE /user/comments/10
 │
 ▼
Requires MODERATOR or ADMIN
 │
 ▼
AccessDeniedException
 │
 ▼
CustomAccessDeniedHandler
 │
 ▼
403
```

Example response:

```json
{
    "success": false,
    "message": "You do not have permission to access this resource",
    "data": null
}
```

The custom handler can also log:

```text
403 FORBIDDEN | method=DELETE | uri=/user/comments/10
```

---

# 🔄 401 vs 403

The easiest way to remember:

```text
401
 ↓
"I don't know who you are."

403
 ↓
"I know who you are,
but you are not allowed to do this."
```

Architecture:

```text
                     REQUEST
                        │
                        ▼
                 Authentication
                        │
                ┌───────┴───────┐
                │               │
             Failure          Success
                │               │
                ▼               ▼
               401         Authorization
                                 │
                         ┌───────┴───────┐
                         │               │
                       Allowed         Denied
                         │               │
                         ▼               ▼
                    Controller          403
```

---

# 👑 Role Assignment

A very important security rule is:

> **Users should not normally choose their own privileged role during registration.**

Normal registration:

```text
POST /auth/register

username
password
```

Server:

```text
role = USER
```

Therefore:

```text
New Registration
       │
       ▼
USER
```

A malicious client should not be able to send:

```json
{
    "username": "attacker",
    "password": "password",
    "role": "ADMIN"
}
```

and become an administrator.

---

## How does an ADMIN get created?

Privileged roles should be assigned through a controlled mechanism.

For example:

```text
Existing ADMIN
      │
      ▼
Protected Admin API
      │
      ▼
Change user's role
      │
      ▼
USER → MODERATOR
USER → ADMIN
MODERATOR → USER
```

The role-management endpoint itself should be protected:

```java
@PreAuthorize("hasRole('ADMIN')")
```

This creates a security boundary around role assignment.

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
Password Verification
        │
        ├── FAIL → Authentication Failure
        │
        └── SUCCESS
                │
                ▼
          Authentication
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
   │   Authentication Failure
   │    │
   │    ▼
   │   401
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
   Authorization
        │
        ▼
   Controller
```

---

# 🔐 Complete RBAC Request Architecture

This is the complete flow now implemented by the project:

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
JWT Signature Verification
   │
   ▼
Authentication
   │
   ▼
SecurityContext
   │
   ├── Principal
   └── Authorities
           │
           ├── ROLE_USER
           ├── ROLE_MODERATOR
           └── ROLE_ADMIN
   │
   ▼
Controller Method
   │
   ▼
@PreAuthorize
   │
   ▼
Authorization Decision
   │
   ├── Allowed
   │     │
   │     ▼
   │   Controller
   │     │
   │     ▼
   │    200
   │
   └── Denied
         │
         ▼
   AccessDeniedException
         │
         ▼
   CustomAccessDeniedHandler
         │
         ▼
        403
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

Each request carries a token that can be validated by the server.

However, stateless JWT authentication does **not automatically solve**:

* token revocation
* stolen-token handling
* refresh-token management
* logout invalidation
* token rotation

These are separate architectural concerns.

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

---

## 4. HMAC secret must remain secret

HMAC uses:

```text
Shared Secret
```

Anyone possessing the secret can potentially create valid signatures.

---

## 5. JWT payload is not automatically encrypted

A signed JWT protects integrity/authenticity.

It does not mean:

```text
Payload = encrypted
```

Do not put confidential information into normal signed JWT claims simply because the token is encoded.

---

## 6. Never allow normal registration to choose ADMIN

Bad:

```json
{
    "username": "rajat",
    "password": "password",
    "role": "ADMIN"
}
```

Good:

```text
Registration
     │
     ▼
Server assigns USER
```

Privileged roles should be assigned through controlled administrative operations.

---

## 7. Do not confuse authentication with authorization

```text
JWT valid
    ≠
Access automatically allowed
```

A valid JWT proves/establishes authentication.

RBAC determines whether the authenticated user is allowed to access a particular operation.

---

## 8. Do not confuse 401 and 403

```text
401 → Authentication problem

403 → Authorization problem
```

---

# 🧩 Complete Component Reference

| Component                        | Responsibility                                                  |
| -------------------------------- | --------------------------------------------------------------- |
| `SecurityConfig`                 | Configures Spring Security                                      |
| `JwtKeyConfig`                   | Loads/configures cryptographic keys                             |
| `AuthController`                 | Handles authentication HTTP endpoints                           |
| `UserController`                 | Demonstrates protected RBAC endpoints                           |
| `AuthService`                    | Defines authentication operations                               |
| `AuthServiceImpl`                | Implements authentication operations                            |
| `LoginRequestDTO`                | Carries login request data                                      |
| `LoginResponseDTO`               | Carries login response data                                     |
| `UserEntity`                     | Represents database user                                        |
| `UserRepository`                 | Communicates with database                                      |
| `CustomUserDetailsService`       | Loads users for Spring Security                                 |
| `UserDetails`                    | Spring Security representation of a user                        |
| `DaoAuthenticationProvider`      | Authenticates username/password                                 |
| `AuthenticationManager`          | Coordinates authentication                                      |
| `PasswordEncoder`                | Password hashing/verifying abstraction                          |
| `BCryptPasswordEncoder`          | BCrypt password hashing                                         |
| `SCryptPasswordEncoder`          | SCrypt password hashing                                         |
| `Argon2PasswordEncoder`          | Argon2 password hashing                                         |
| `JwtService`                     | Creates/verifies JWTs                                           |
| `JwtAuthenticationFilter`        | Processes JWTs on requests                                      |
| `SecurityContext`                | Stores current request authentication                           |
| `CustomAuthenticationEntryPoint` | Handles `401 Unauthorized`                                      |
| `CustomAccessDeniedHandler`      | Handles `403 Forbidden`                                         |
| `@EnableMethodSecurity`          | Enables method-level authorization                              |
| `@PreAuthorize`                  | Performs authorization before method execution                  |
| `private_key.pem`                | Signs asymmetric JWTs                                           |
| `public_key.pem`                 | Verifies asymmetric JWTs                                        |
| HMAC secret                      | Signs/verifies HMAC JWTs                                        |
| Bouncy Castle                    | Provides cryptographic classes required by Argon2 in this setup |

---

# 🧠 Three Layers of Security

It is useful to think about the application as three major layers.

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

## Layer 3 — Token + Authorization

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
      │
      ▼
JwtAuthenticationFilter
      │
      ▼
SecurityContext
      │
      ▼
Roles / Authorities
      │
      ▼
@PreAuthorize
      │
      ▼
Authorization Decision
```

---

# 📊 Complete Security Technology Map

```text
                         SPRING SECURITY
                               │
       ┌───────────────────────┼───────────────────────┐
       │                       │                       │
       ▼                       ▼                       ▼
  Password Security       Authentication          Authorization
       │                       │                       │
       ▼                       ▼                       ▼
PasswordEncoder       AuthenticationManager         RBAC
       │                       │                       │
  ┌────┼────┐                 ▼                  ┌────┼────┐
  │    │    │        DaoAuthentication            │    │    │
  ▼    ▼    ▼           Provider                  ▼    ▼    ▼
BCrypt SCrypt Argon2          │                 USER MOD ADMIN
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

There are multiple independent security concerns in this project.

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

Protects stored passwords.

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

Protects JWT integrity/authenticity.

---

## Authorization

```text
Authenticated User
       │
       ▼
Role / Authority
       │
       ▼
Authorization Rule
       │
       ▼
Allowed / Forbidden
```

Controls what the authenticated user can access.

---

# 🔥 Final Comparison

| Technology                 | Category                | Purpose                       | Key / Security Information |
| -------------------------- | ----------------------- | ----------------------------- | -------------------------- |
| BCrypt                     | Password hashing        | Store/verify passwords        | Salt + parameters          |
| SCrypt                     | Password hashing        | Store/verify passwords        | Salt + parameters          |
| Argon2                     | Password hashing        | Store/verify passwords        | Salt + parameters          |
| HMAC                       | Symmetric cryptography  | JWT signing/verification      | Shared secret              |
| RSA                        | Asymmetric cryptography | JWT signing/verification      | Private + Public key       |
| ECDSA                      | Asymmetric cryptography | JWT signing/verification      | Private + Public key       |
| RBAC                       | Authorization           | Control resource access       | Roles/authorities          |
| `@PreAuthorize`            | Method security         | Enforce authorization rules   | Spring authorities         |
| `AuthenticationEntryPoint` | Exception handling      | Handle authentication failure | 401                        |
| `AccessDeniedHandler`      | Exception handling      | Handle authorization failure  | 403                        |

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
10. Bouncy Castle Dependency
       ↓
11. DaoAuthenticationProvider
       ↓
12. AuthenticationManager
       ↓
13. SecurityConfig
       ↓
14. Authentication
       ↓
15. JWT
       ↓
16. HMAC
       ↓
17. RSA
       ↓
18. ECDSA
       ↓
19. JwtService
       ↓
20. JwtAuthenticationFilter
       ↓
21. SecurityContext
       ↓
22. Roles
       ↓
23. Authorities
       ↓
24. RBAC
       ↓
25. @EnableMethodSecurity
       ↓
26. @PreAuthorize
       ↓
27. 401 vs 403
       ↓
28. Custom Exception Handling
       ↓
29. Role Management
       ↓
30. Permission-Based Authorization
```

---

# 🧠 Final Mental Model

If you remember only one architecture from this project, remember this:

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
                                      │
                                      ▼
                              Bouncy Castle
                          (required dependency
                           in this setup)
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
                                      ├── Username
                                      ├── Password
                                      └── Role
                                           │
                                           ▼
                                      Authorities
                                           │
                                           ├── ROLE_USER
                                           ├── ROLE_MODERATOR
                                           └── ROLE_ADMIN
                                      │
                                      ▼
                              Password Verification
                                      │
                               ┌──────┴──────┐
                               │             │
                              NO            YES
                               │             │
                               ▼             ▼
                       Authentication   Authentication
                           Failure          Success
                               │             │
                               ▼             ▼
                              401        JwtService
                                             │
                                             ▼
                                      JWT Signing
                                             │
                              ┌──────────────┼──────────────┐
                              │              │              │
                              ▼              ▼              ▼
                            HMAC            RSA           ECDSA
                              │              │              │
                         Secret Key     Private Key    Private Key
                              │              │              │
                              └──────────────┼──────────────┘
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
                                             ▼
                                    Signature Verification
                                             │
                              ┌──────────────┴──────────────┐
                              │                             │
                             NO                            YES
                              │                             │
                              ▼                             ▼
                         401 Unauthorized             Authentication
                         EntryPoint                       │
                                                          ▼
                                                   SecurityContext
                                                          │
                                                          ▼
                                                   Authorization
                                                          │
                                                          ▼
                                                   @PreAuthorize
                                                          │
                                             ┌────────────┴────────────┐
                                             │                         │
                                          Allowed                    Denied
                                             │                         │
                                             ▼                         ▼
                                        Controller            AccessDeniedException
                                             │                         │
                                             ▼                         ▼
                                           200                  CustomAccessDeniedHandler
                                                                       │
                                                                       ▼
                                                                      403
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

Uses SCrypt for memory-hard password hashing.

### `Argon2PasswordEncoder`

Uses Argon2 for modern memory-hard password hashing.

### Bouncy Castle

Provides cryptographic classes required by the Argon2 implementation used in this project.

Dependency:

```xml
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk18on</artifactId>
    <version>1.86</version>
</dependency>
```

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

### RBAC

Controls access to application resources based on user roles.

Current roles:

```text
USER
MODERATOR
ADMIN
```

### `@PreAuthorize`

Performs authorization checks before a controller/service method executes.

Examples:

```java
@PreAuthorize("hasRole('ADMIN')")
```

and:

```java
@PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
```

### `AuthenticationEntryPoint`

Handles authentication failures and produces `401 Unauthorized`.

### `AccessDeniedHandler`

Handles authorization failures and produces `403 Forbidden`.

### Private Key

Signs asymmetric JWTs.

### Public Key

Verifies asymmetric JWT signatures.

---

# 🛠️ Project Purpose

This repository is intentionally built as a **Spring Security learning project**.

The goal is to understand the complete security pipeline:

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
Roles / Authorities
   ↓
RBAC
   ↓
ABAC Policy
   ↓
@PreAuthorize
   ↓
Protected Controller / Service
```

And, at the cryptographic level:

```text
PASSWORD SECURITY
        │
        ├── BCrypt
        ├── SCrypt
        └── Argon2
                  │
                  └── Bouncy Castle


JWT SECURITY
        │
        ├── HMAC
        ├── RSA
        └── ECDSA


AUTHORIZATION
        │
        ├── RBAC
        │     ├── USER
        │     ├── MODERATOR
        │     └── ADMIN
        │
        └── ABAC
              ├── Subject attributes
              ├── Resource attributes
              ├── Action
              └── Environment/context
```

The most important thing is to understand **why each component exists, what problem it solves, and who calls it**, rather than memorizing Spring Security configuration from tutorials.
