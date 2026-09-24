# 🔐 Learn Spring Security

> A practical Spring Security project created **just for learning and understanding how Spring Security works internally**.

This project focuses on understanding:

* Spring Security architecture
* `UserDetailsService`
* `UserDetails`
* `DaoAuthenticationProvider`
* `AuthenticationManager`
* `PasswordEncoder`
* BCrypt password hashing
* JWT authentication
* JWT filters
* RSA key-pair signing
* ECDSA key-pair signing
* `SecurityContext`
* Stateless authentication
* Authentication vs Authorization

---

# 📚 Table of Contents

1. [Project Goal](#-project-goal)
2. [Spring Security Authentication Concepts](#-spring-security-authentication-concepts)
3. [Important Spring Security Interfaces](#-important-spring-security-interfaces)
4. [Complete Project Architecture](#-complete-project-architecture)
5. [File-by-File Explanation](#-file-by-file-explanation)
6. [Registration Flow](#-registration-flow)
7. [Login Flow](#-login-flow)
8. [How BCrypt Works](#-how-bcrypt-works)
9. [JWT Authentication Flow](#-jwt-authentication-flow)
10. [JWT Filter](#-jwt-authentication-filter)
11. [SecurityContext](#-securitycontext)
12. [RSA](#-rsa)
13. [ECDSA](#-ecdsa)
14. [RSA vs ECDSA](#-rsa-vs-ecdsa)
15. [HMAC vs RSA vs ECDSA](#-hmac-vs-rsa-vs-ecdsa)
16. [Complete Request Flow](#-complete-request-flow)
17. [Important Concepts to Remember](#-important-concepts-to-remember)

---

# 🎯 Project Goal

The goal of this project is **not simply to implement login**.

The goal is to understand what happens internally when a user:

1. Registers
2. Stores a password
3. Logs in
4. Gets authenticated
5. Receives a JWT
6. Sends the JWT with another request
7. Gets verified
8. Reaches a protected controller

The complete authentication architecture can be visualized as:

```text
                 USER
                  │
                  ▼
              REGISTER
                  │
                  ▼
          BCryptPasswordEncoder
                  │
                  ▼
             PostgreSQL
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
              Database
                  │
                  ▼
             UserDetails
                  │
                  ▼
               BCrypt
                  │
          Password matches?
             │         │
            NO        YES
             │         │
            401        ▼
                  JwtService
                       │
                       ▼
                 Private Key
                       │
                       ▼
                     JWT
```

---

# 🔐 Spring Security Authentication Concepts

## `UserDetailsService`

`UserDetailsService` is the Spring Security service responsible for **loading user information**.

Spring Security needs information such as:

* username
* password
* authorities/roles
* account status

Instead of allowing Spring Security to know how our database works, we create our own implementation:

```java
@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(
            UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        UserEntity user =
                userRepository.findByUsername(username)
                    .orElseThrow(() ->
                        new UsernameNotFoundException(
                            "User not found"
                        )
                    );

        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }
}
```

### What does it do?

It acts as the bridge:

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

Spring Security does **not** directly know how to query our `UserEntity`.

Our `CustomUserDetailsService` tells Spring:

> "If you give me a username, I will find that user's security information."

---

# 👤 `UserDetails`

`UserDetails` is an interface used by Spring Security to represent the authenticated user's security information.

It contains information such as:

```text
Username
Password
Authorities
Account status
Account locked status
Account expired status
Credentials expired status
```

Example:

```java
UserDetails userDetails =
        User.withUsername("rajat")
                .password(password)
                .roles("USER")
                .build();
```

Here:

```text
UserDetails
     │
     └── Spring Security representation of the user
```

Our database entity:

```text
UserEntity
```

is our application's representation of the user.

`UserDetails` is Spring Security's representation.

---

# 🔑 Password Encoding

Older Spring Security examples sometimes use:

```text
{noop}password
```

`{noop}` tells Spring Security:

> Do not apply a password encoder to this password.

For example:

```text
{noop}ourpassword
```

means the actual password is:

```text
ourpassword
```

However, this should **not be used for real applications**.

This project uses:

```text
BCrypt
```

instead.

The important distinction is:

```text
Password
   │
   ▼
BCryptPasswordEncoder
   │
   ▼
Hashed password
   │
   ▼
Database
```

BCrypt does **not** generate JWTs.

BCrypt is responsible for **password hashing and password verification**.

---

# 🏗️ Complete Project Architecture

A typical structure for this project is:

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

## 1. `SecurityConfig.java`

### Location

```text
config/SecurityConfig.java
```

### Responsibility

This is the **main Spring Security configuration file**.

It tells Spring Security:

* which endpoints are public
* which endpoints require authentication
* which authentication provider to use
* which password encoder to use
* how the `AuthenticationManager` is created
* where the JWT filter is placed

Typical responsibilities:

```text
SecurityConfig
     │
     ├── PasswordEncoder
     │
     ├── AuthenticationProvider
     │
     ├── AuthenticationManager
     │
     ├── SecurityFilterChain
     │
     └── JwtAuthenticationFilter
```

Example:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Authentication provider:

```java
@Bean
public AuthenticationProvider authenticationProvider(
        UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder) {

    DaoAuthenticationProvider provider =
            new DaoAuthenticationProvider(userDetailsService);

    provider.setPasswordEncoder(passwordEncoder);

    return provider;
}
```

Authentication manager:

```java
@Bean
public AuthenticationManager authenticationManager(
        AuthenticationConfiguration configuration)
        throws Exception {

    return configuration.getAuthenticationManager();
}
```

---

# 2. `JwtKeyConfig.java`

### Location

```text
config/JwtKeyConfig.java
```

### Responsibility

This class loads the cryptographic keys used by JWT.

For RSA:

```text
private_key.pem
       │
       ▼
PrivateKey
```

and:

```text
public_key.pem
       │
       ▼
PublicKey
```

For ECDSA, the same architecture is used, but the keys are EC keys.

The private key is used for:

```text
JWT SIGNING
```

The public key is used for:

```text
JWT VERIFICATION
```

---

# 3. `AuthController.java`

### Location

```text
controller/AuthController.java
```

### Responsibility

This is the HTTP layer.

It receives requests such as:

```http
POST /auth/login
```

and:

```http
POST /auth/register
```

The controller should ideally remain thin.

Example:

```text
HTTP Request
     │
     ▼
AuthController
     │
     ▼
AuthService
```

It should not contain all authentication logic.

---

# 4. `LoginRequestDTO.java`

### Location

```text
dto/LoginRequestDTO.java
```

### Responsibility

Represents login input.

Example JSON:

```json
{
    "username": "rajat",
    "password": "ourpassword"
}
```

DTO:

```text
LoginRequestDTO
├── username
└── password
```

---

# 5. `LoginResponseDTO.java`

### Location

```text
dto/LoginResponseDTO.java
```

### Responsibility

Represents the response returned after successful authentication.

For example:

```json
{
    "token": "eyJhbGciOiJFUzI1NiJ9..."
}
```

The DTO keeps the API response structure separate from internal security classes.

---

# 6. `UserEntity.java`

### Location

```text
entity/UserEntity.java
```

### Responsibility

Represents the user stored in the database.

Example:

```text
UserEntity
├── id
├── username
└── password
```

The important part is:

```java
password
```

The database should contain the **hashed password**, not the plain-text password.

Example:

```text
User enters:

ourpassword

        │
        ▼

BCrypt

        │
        ▼

$2a$10$.....................

        │
        ▼

Database
```

---

# 7. `UserRepository.java`

### Location

```text
repository/UserRepository.java
```

### Responsibility

Responsible for communicating with the database.

For example:

```java
Optional<UserEntity> findByUsername(String username);
```

The authentication flow eventually reaches this repository.

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

# 8. `AuthService.java`

### Location

```text
service/AuthService.java
```

### Responsibility

Defines authentication-related business operations.

For example:

```java
void register(RegisterRequestDTO request);

LoginResponseDTO login(LoginRequestDTO request);
```

This is an interface that defines what authentication operations are available.

---

# 9. `AuthServiceImpl.java`

### Location

```text
service/AuthServiceImpl.java
```

### Responsibility

Contains the implementation of authentication business logic.

For registration:

```text
Request
  │
  ▼
AuthServiceImpl
  │
  ▼
BCryptPasswordEncoder
  │
  ▼
UserRepository
  │
  ▼
Database
```

For login:

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

# 10. `CustomUserDetailsService.java`

### Location

```text
security/CustomUserDetailsService.java
```

### Responsibility

This is one of the most important classes in the project.

It implements:

```java
UserDetailsService
```

Its job is:

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

It converts:

```text
Application User
```

into:

```text
Spring Security UserDetails
```

---

# 11. `JwtService.java`

### Location

```text
security/JwtService.java
```

### Responsibility

Responsible for JWT operations.

It can:

* generate JWT
* sign JWT
* extract claims
* verify JWT signature
* read username/subject
* check expiration

For ECDSA:

```text
PrivateKey
    │
    ▼
JwtService
    │
    ▼
Sign JWT
```

For verification:

```text
JWT
 │
 ▼
JwtService
 │
 ▼
PublicKey
 │
 ▼
Verify signature
```

---

# 12. `JwtAuthenticationFilter.java`

### Location

```text
security/JwtAuthenticationFilter.java
```

### Responsibility

This filter processes JWTs on incoming requests.

For example:

```http
Authorization: Bearer eyJhbGciOiJFUzI1NiJ9...
```

The filter:

1. Reads the `Authorization` header.
2. Extracts the Bearer token.
3. Validates the JWT.
4. Extracts the username.
5. Loads the user.
6. Creates an `Authentication` object.
7. Stores it inside the `SecurityContext`.

Flow:

```text
HTTP Request
     │
     ▼
Authorization Header
     │
     ▼
JwtAuthenticationFilter
     │
     ▼
JWT
     │
     ▼
Public Key
     │
     ▼
Signature Valid?
     │
 ┌───┴────┐
NO       YES
 │         │
 ▼         ▼
401    UserDetails
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
BCryptPasswordEncoder
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

Important:

```text
Plain password
       ❌
       │
       ▼
Database
```

Instead:

```text
Plain password
       │
       ▼
BCrypt
       │
       ▼
Hash
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
BCrypt
  │
  │ password matches?
  ▼
Authentication SUCCESS
  │
  ▼
JwtService
  │
  ▼
Private Key
  │
  ▼
JWT
```

---

# 🔐 How BCrypt Works

Suppose the user enters:

```text
ourpassword
```

During registration:

```text
ourpassword
      │
      ▼
BCryptPasswordEncoder
      │
      ▼
$2a$10$....................
      │
      ▼
Database
```

During login:

```text
Entered password
      │
      ▼
BCryptPasswordEncoder.matches()
      │
      ▼
Stored BCrypt hash
      │
      ▼
true / false
```

BCrypt is **one-way hashing**.

You do not decrypt the BCrypt hash.

You verify whether the supplied password matches the stored hash.

---

# 🎟️ JWT Authentication

After successful login, the application generates a JWT.

Conceptually:

```text
UserDetails
     │
     ▼
JwtService
     │
     ▼
Private Key
     │
     ▼
Signed JWT
```

The JWT contains information such as:

```json
{
    "sub": "rajat",
    "iat": 1790106334,
    "exp": 1790109934
}
```

The exact claims depend on what the application adds.

---

# 🛡️ JWT Authentication Request

After login, the client sends:

```http
Authorization: Bearer <JWT>
```

The request enters the Spring Security filter chain.

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
Public Key
  │
  ▼
Signature Verification
```

If the signature is invalid:

```text
401 Unauthorized
```

If valid:

```text
SecurityContext
      │
      ▼
Authentication
      │
      ▼
Controller
```

---

# 🧠 SecurityContext

`SecurityContext` contains the authentication information for the current request.

Conceptually:

```text
SecurityContext
       │
       ▼
Authentication
       │
       ├── Principal
       ├── Authorities
       └── Authentication status
```

After the JWT is successfully validated:

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

Then the controller can access the authenticated user.

---

# 🔐 RSA

RSA uses an asymmetric key pair:

```text
Private Key
+
Public Key
```

The private key signs the JWT.

The public key verifies the JWT.

---

## Generate a 3072-bit RSA Private Key

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:3072 -out private_key.pem
```

This generates:

```text
private_key.pem
```

---

## Generate Public Key

```bash
openssl rsa -pubout -in private_key.pem -out public_key.pem
```

Now:

```text
private_key.pem
        │
        │ generates
        ▼
public_key.pem
```

---

# 🔑 RSA Architecture

```text
             ┌─────────────────────────┐
             │       RSA KEY PAIR      │
             │                         │
             │   private_key.pem       │
             │   public_key.pem        │
             └────────────┬────────────┘
                          │
                   JwtKeyConfig
                          │
             ┌────────────┴────────────┐
             ▼                         ▼
        PrivateKey                  PublicKey
             │                         │
           SIGN                      VERIFY
             │                         │
             ▼                         ▼
        JwtService          JwtAuthenticationFilter
             │                         │
             ▼                         │
            JWT ──────────────────────┘
                                      │
                                      ▼
                              SecurityContext
                                      │
                                      ▼
                                  Controller
```

---

# 🧬 ECDSA

ECDSA is another asymmetric cryptographic algorithm.

Instead of RSA:

```text
RSA Private Key
RSA Public Key
```

we use:

```text
EC Private Key
EC Public Key
```

For ES256, the algorithm uses:

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

Using the P-256 curve:

```bash
openssl genpkey -algorithm EC -pkeyopt ec_paramgen_curve:P-256 -out private_key.pem
```

This creates:

```text
private_key.pem
```

---

# 🔑 Generate ECDSA Public Key

```bash
openssl ec -in private_key.pem -pubout -out public_key.pem
```

Now:

```text
private_key.pem
       │
       │
       ▼
public_key.pem
```

---

# 🧬 ECDSA Architecture

```text
             ┌─────────────────────────┐
             │       EC KEY PAIR      │
             │                         │
             │   private_key.pem       │
             │   public_key.pem        │
             └────────────┬────────────┘
                          │
                   JwtKeyConfig
                          │
             ┌────────────┴────────────┐
             ▼                         ▼
        PrivateKey                  PublicKey
             │                         │
           SIGN                      VERIFY
             │                         │
             ▼                         ▼
        JwtService          JwtAuthenticationFilter
             │                         │
             ▼                         │
            JWT ──────────────────────┘
                                      │
                                      ▼
                              SecurityContext
                                      │
                                      ▼
                                  Controller
```

---

# 🆚 RSA vs ECDSA

| Feature        | RSA              | ECDSA             |
| -------------- | ---------------- | ----------------- |
| JWT Algorithm  | RS256            | ES256             |
| Cryptography   | RSA              | Elliptic Curve    |
| Keys           | Private + Public | Private + Public  |
| Signing        | Private key      | Private key       |
| Verification   | Public key       | Public key        |
| Curve          | Not applicable   | P-256 for ES256   |
| Key size       | Generally larger | Generally smaller |
| Signature size | Generally larger | Generally smaller |
| JWT support    | Very common      | Very common       |

---

# 🔥 HMAC vs RSA vs ECDSA

| Part                      | HMAC         | RSA          | ECDSA        |
| ------------------------- | ------------ | ------------ | ------------ |
| JWT Algorithm             | HS256        | RS256        | ES256        |
| Password hashing          | BCrypt       | BCrypt       | BCrypt       |
| JWT signing               | Secret key   | Private key  | Private key  |
| JWT verification          | Same secret  | Public key   | Public key   |
| `JwtService`              | HMAC signing | RSA signing  | EC signing   |
| Shared secret             | Required     | Not required | Not required |
| Private key               | ❌            | Required     | Required     |
| Public key                | ❌            | Required     | Required     |
| AuthenticationManager     | Same         | Same         | Same         |
| CustomUserDetailsService  | Same         | Same         | Same         |
| DaoAuthenticationProvider | Same         | Same         | Same         |
| BCrypt                    | Same         | Same         | Same         |

### Important

Changing:

```text
HS256 → RS256 → ES256
```

does **not** change how Spring authenticates the username/password.

This part remains the same:

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
Database
        │
        ▼
BCrypt
```

Only the JWT cryptographic signing/verification mechanism changes.

---

# 🔄 Complete Login Architecture

The entire application can be understood as two separate stages.

## Stage 1 — Username/Password Authentication

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
BCrypt
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
Private Key
        │
        ▼
Signed JWT
        │
        ▼
Client
```

---

# 🔄 Subsequent API Request

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
Public Key
   │
   ▼
Signature Valid?
   │
   ├─────────────── NO ───────────────► 401
   │
   ▼
YES
   │
   ▼
UserDetails
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

# 🧩 What Each Major Component Does

| Component                   | Main Responsibility                          |
| --------------------------- | -------------------------------------------- |
| `SecurityConfig`            | Configures Spring Security                   |
| `JwtKeyConfig`              | Creates/loads JWT cryptographic keys         |
| `AuthController`            | Handles authentication HTTP endpoints        |
| `AuthService`               | Defines authentication business operations   |
| `AuthServiceImpl`           | Implements authentication operations         |
| `LoginRequestDTO`           | Carries login request data                   |
| `LoginResponseDTO`          | Carries login response data                  |
| `UserEntity`                | Represents database user                     |
| `UserRepository`            | Communicates with user database              |
| `CustomUserDetailsService`  | Loads users for Spring Security              |
| `UserDetails`               | Spring Security representation of a user     |
| `DaoAuthenticationProvider` | Performs username/password authentication    |
| `AuthenticationManager`     | Coordinates authentication                   |
| `PasswordEncoder`           | Hashes/verifies passwords                    |
| `BCryptPasswordEncoder`     | BCrypt implementation of password hashing    |
| `JwtService`                | Generates and validates JWTs                 |
| `JwtAuthenticationFilter`   | Processes JWTs on incoming requests          |
| `SecurityContext`           | Holds authentication for the current request |
| `private_key.pem`           | Signs JWT                                    |
| `public_key.pem`            | Verifies JWT                                 |

---

# 🧠 Most Important Concept

There are actually **two different security processes** happening.

## Process 1 — Login Authentication

```text
Who are you?
     │
     ▼
Username + Password
     │
     ▼
Database
     │
     ▼
BCrypt
     │
     ▼
Authentication
```

## Process 2 — JWT Authentication

```text
Are you still authenticated?
     │
     ▼
JWT
     │
     ▼
Public Key
     │
     ▼
Signature verification
     │
     ▼
SecurityContext
```

These are related, but they are **not the same thing**.

---

# 🚨 Important Security Rules

## Never store plain-text passwords

Bad:

```text
password = "ourpassword"
```

Good:

```text
password = BCrypt hash
```

---

## Never expose the private key

The private key should remain on the backend.

```text
Backend
  │
  └── private_key.pem
```

The frontend/client should never receive the private key.

The public key can be distributed for verification where appropriate.

---

# 🔐 Key Responsibility

## Private Key

```text
PRIVATE KEY
     │
     ▼
SIGN
     │
     ▼
JWT
```

## Public Key

```text
JWT
 │
 ▼
PUBLIC KEY
 │
 ▼
VERIFY
```

Therefore:

```text
Private key = Signing
Public key  = Verification
```

---

# 🧠 Final Mental Model

If you remember only one diagram from this project, remember this:

```text
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
                        BCrypt
                           │
                           ▼
                  Authentication Success
                           │
                           ▼
                       JwtService
                           │
                           ▼
                     Private Key
                           │
                           ▼
                          JWT
                           │
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
                      Public Key
                           │
                           ▼
                  Signature Valid?
                     /           \
                   NO             YES
                   │               │
                   ▼               ▼
                  401        SecurityContext
                                   │
                                   ▼
                              Controller
```

---

# 📌 Final Summary

### `UserDetailsService`

Loads the user from the application's data source.

```text
Database → UserDetailsService → UserDetails
```

### `UserDetails`

Represents the user's security information inside Spring Security.

### `DaoAuthenticationProvider`

Uses `UserDetailsService` and `PasswordEncoder` to authenticate username/password credentials.

### `AuthenticationManager`

Starts and coordinates the authentication process.

### `BCryptPasswordEncoder`

Hashes passwords during registration and verifies passwords during login.

### `JwtService`

Creates and verifies JWTs.

### `JwtAuthenticationFilter`

Reads JWTs from incoming requests and establishes authentication.

### `SecurityContext`

Stores the authenticated user's information for the current request.

### Private Key

Signs JWTs.

### Public Key

Verifies JWT signatures.

---

# 🚀 Learning Order

For understanding this project properly, learn the components in this order:

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
7. BCryptPasswordEncoder
       ↓
8. DaoAuthenticationProvider
       ↓
9. AuthenticationManager
       ↓
10. SecurityConfig
       ↓
11. JwtService
       ↓
12. JWT
       ↓
13. JwtAuthenticationFilter
       ↓
14. SecurityContext
       ↓
15. RSA / ECDSA
```

The most important thing is to understand **why each component exists and who calls it**, rather than memorizing the configuration.

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
JWT
   ↓
JwtAuthenticationFilter
   ↓
SecurityContext
   ↓
Protected Controller
```

Once this flow is clear, Spring Security configuration becomes much easier to understand instead of feeling like a collection of annotations and configuration copied from tutorials.
