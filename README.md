# learn_spring_security
Just to learn spring security

# Configuration 

I used two services of spring security and they are UserDetailsService and UserDetails 

<ul>

<li>

<b>
UserDetailsService :
</b> this is the main service which retrieves security information about users. As we defined our custom bean, we don't see anymore in the log the default password provided by Spring, as we are now providing it. 
</li>

<li>
<b>
UserDetails 
</b> class : this is the interface which Spring uses to process user's security related information, like the username or its password. We are using the Standard User class as its implementation. 
</li>
</ul>

<p> 
The password is prefixed with <i> {noop} </i> to avoid a password encoder, so that our real password is  <b> <i> ourpassword </i> </b>.  
</p>


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
                    BCrypt
                      │
                      ▼
              Authentication OK
                      │
                      ▼
                 JwtService
                      │
              RSA Private Key
                      │
                      ▼
                    JWT


Later, for an API request:

Client
│
│ Authorization: Bearer <JWT>
▼
Resource Server
│
▼
RSA Public Key
│
▼
Signature valid?
│
├── NO  → 401
│
└── YES
│
▼
SecurityContext
│
▼
Controller

|                     | RSA            | EC              |
| ------------------- | -------------- | --------------- |
| Algorithm           | RS256          | ES256           |
| Keys                | Private/Public | Private/Public  |
| Signing             | Private key    | Private key     |
| Verification        | Public key     | Public key      |
| Learning difficulty | Easier         | Slightly harder |
| Common JWT use      | Very common    | Very common     |
| Key size            | Larger         | Smaller         |
| Signature size      | Larger         | Smaller         |

| Part                               | HMAC         | RSA                 |
| ---------------------------------- | ------------ | ------------------- |
| Password hashing                   | BCrypt       | **Same BCrypt**     |
| JWT signing                        | Secret key   | **Private RSA key** |
| JWT verification                   | Same secret  | **Public RSA key**  |
| `JwtService`                       | HMAC signing | RSA signing         |
| Secret in `application.properties` | `jwt.secret` | **Not needed**      |
| Private key                        | ❌            | **Required**        |
| Public key                         | ❌            | **Required**        |
| AuthenticationManager              | Same         | **Same**            |
| CustomUserDetailsService           | Same         | **Same**            |
| DaoAuthenticationProvider          | Same         | **Same**            |

# Generate a 3072-bit RSA private key

openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:3072 -out private_key.pem

# Generate public key from Private Key

openssl rsa -pubout -in private_key.pem -out public_key.pem

So Now : 

private_key.pem
│
│ generates
▼
public_key.pem