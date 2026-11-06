
#!/usr/bin/env bash

set -euo pipefail

# ==========================================
# Spring Security - mTLS Certificate Generator
# Architecture: Single Spring Boot Monolith
# Algorithm: RSA 2048 / RSA 4096 CA
# ==========================================

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

OUT="$ROOT/secrets/mtls"

CA_DIR="$OUT/ca"
SERVER_DIR="$OUT/server"
CLIENT_DIR="$OUT/client"
TRUSTSTORE_DIR="$OUT/truststore"

# Development password.
# Override using CERT_PASSWORD environment variable.
PASS="${CERT_PASSWORD:-changeit}"
export CERT_PASSWORD="$PASS"

echo "=========================================="
echo "Spring Security mTLS Certificate Setup"
echo "=========================================="

# Check dependencies
command -v openssl >/dev/null 2>&1 || {
    echo "ERROR: OpenSSL is not installed."
    exit 1
}

command -v keytool >/dev/null 2>&1 || {
    echo "ERROR: Java keytool is not available."
    exit 1
}

# Prevent accidental overwriting of existing keys.
if [[ -e "$CA_DIR/ca.key" ||
      -e "$SERVER_DIR/server.key" ||
      -e "$CLIENT_DIR/client.key" ]]; then

    echo "ERROR: Existing private keys found."
    echo "Back up existing secrets before regenerating."
    exit 1
fi

# Create directories
mkdir -p "$CA_DIR" "$SERVER_DIR" "$CLIENT_DIR" "$TRUSTSTORE_DIR"

# Temporary working directory
TEMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TEMP_DIR"' EXIT

# ==========================================
# STEP 1: Generate Certificate Authority
# ==========================================

echo "[1/6] Generating Certificate Authority..."

openssl genrsa \
    -out "$CA_DIR/ca.key" \
    4096

openssl req -x509 \
    -new \
    -key "$CA_DIR/ca.key" \
    -sha256 \
    -days 3650 \
    -subj "/C=IN/ST=Chhattisgarh/O=SpringSecurityLearning/CN=SpringSecurityLocalCA" \
    -out "$CA_DIR/ca.crt" \
    -addext "basicConstraints=critical,CA:TRUE,pathlen:0" \
    -addext "keyUsage=critical,keyCertSign,cRLSign" \
    -addext "subjectKeyIdentifier=hash"

# ==========================================
# STEP 2: Generate Backend Server Certificate
# ==========================================

echo "[2/6] Generating backend server certificate..."

openssl genrsa \
    -out "$SERVER_DIR/server.key" \
    2048

openssl req -new \
    -key "$SERVER_DIR/server.key" \
    -subj "/C=IN/ST=Chhattisgarh/O=SpringSecurityLearning/CN=localhost" \
    -out "$TEMP_DIR/server.csr"

cat > "$TEMP_DIR/server.ext" <<'EOF'
basicConstraints=critical,CA:FALSE
keyUsage=critical,digitalSignature,keyEncipherment
extendedKeyUsage=serverAuth
subjectAltName=DNS:localhost,IP:127.0.0.1
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
EOF

openssl x509 -req \
    -in "$TEMP_DIR/server.csr" \
    -CA "$CA_DIR/ca.crt" \
    -CAkey "$CA_DIR/ca.key" \
    -CAcreateserial \
    -CAserial "$CA_DIR/ca.srl" \
    -out "$SERVER_DIR/server.crt" \
    -days 825 \
    -sha256 \
    -extfile "$TEMP_DIR/server.ext"

# ==========================================
# STEP 3: Generate Client Certificate
# ==========================================

echo "[3/6] Generating client certificate..."

openssl genrsa \
    -out "$CLIENT_DIR/client.key" \
    2048

openssl req -new \
    -key "$CLIENT_DIR/client.key" \
    -subj "/C=IN/ST=Chhattisgarh/O=SpringSecurityLearning/CN=spring-security-client" \
    -out "$TEMP_DIR/client.csr"

cat > "$TEMP_DIR/client.ext" <<'EOF'
basicConstraints=critical,CA:FALSE
keyUsage=critical,digitalSignature
extendedKeyUsage=clientAuth
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
EOF

openssl x509 -req \
    -in "$TEMP_DIR/client.csr" \
    -CA "$CA_DIR/ca.crt" \
    -CAkey "$CA_DIR/ca.key" \
    -CAserial "$CA_DIR/ca.srl" \
    -out "$CLIENT_DIR/client.crt" \
    -days 825 \
    -sha256 \
    -extfile "$TEMP_DIR/client.ext"

# ==========================================
# STEP 4: Generate Server PKCS12 Keystore
# ==========================================

echo "[4/6] Generating server PKCS12 keystore..."

openssl pkcs12 -export \
    -name spring-security-server \
    -inkey "$SERVER_DIR/server.key" \
    -in "$SERVER_DIR/server.crt" \
    -certfile "$CA_DIR/ca.crt" \
    -out "$SERVER_DIR/server.p12" \
    -passout "pass:$PASS"

# ==========================================
# STEP 5: Generate Client PKCS12 Keystore
# ==========================================

echo "[5/6] Generating client PKCS12 keystore..."

openssl pkcs12 -export \
    -name spring-security-client \
    -inkey "$CLIENT_DIR/client.key" \
    -in "$CLIENT_DIR/client.crt" \
    -certfile "$CA_DIR/ca.crt" \
    -out "$CLIENT_DIR/client.p12" \
    -passout "pass:$PASS"

# ==========================================
# STEP 6: Generate Truststore
# ==========================================

echo "[6/6] Generating CA truststore..."

keytool -importcert \
    -noprompt \
    -alias spring-security-local-ca \
    -file "$CA_DIR/ca.crt" \
    -keystore "$TRUSTSTORE_DIR/truststore.p12" \
    -storetype PKCS12 \
    -storepass "$PASS"

# ==========================================
# Set file permissions
# ==========================================

chmod 600 \
    "$CA_DIR/ca.key" \
    "$SERVER_DIR/server.key" \
    "$CLIENT_DIR/client.key" \
    "$SERVER_DIR/server.p12" \
    "$CLIENT_DIR/client.p12" \
    "$TRUSTSTORE_DIR/truststore.p12"

# ==========================================
# Verify certificates
# ==========================================

echo ""
echo "Verifying server certificate..."

openssl verify \
    -CAfile "$CA_DIR/ca.crt" \
    -purpose sslserver \
    "$SERVER_DIR/server.crt"

echo "Verifying client certificate..."

openssl verify \
    -CAfile "$CA_DIR/ca.crt" \
    -purpose sslclient \
    "$CLIENT_DIR/client.crt"

echo ""
echo "=========================================="
echo "mTLS certificates generated successfully!"
echo "=========================================="

echo "CA:         $CA_DIR"
echo "Server:     $SERVER_DIR"
echo "Client:     $CLIENT_DIR"
echo "Truststore: $TRUSTSTORE_DIR"

echo ""
echo "IMPORTANT:"
echo "1. These certificates are for local development."
echo "2. Never push secrets/ to GitHub."
echo "3. Change the default password before deployment."
