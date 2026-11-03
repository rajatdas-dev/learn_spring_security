#!/usr/bin/env bash

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/certificates"

mkdir -p "$OUT"
cd "$OUT"

PASS="${CERT_PASSWORD:-changeit}"

# 1. Create local certificate authority.
openssl genrsa -out ca.key 4096

openssl req -x509 -new -nodes \
  -key ca.key \
  -sha256 \
  -days 3650 \
  -subj "/CN=SpringSecurityLocalCA" \
  -out ca.crt

# 2. Create backend server certificate.
openssl genrsa -out backend-server.key 2048

openssl req -new \
  -key backend-server.key \
  -subj "/CN=localhost" \
  -out backend-server.csr

cat > backend-server.ext <<'EOF'
subjectAltName=DNS:localhost,IP:127.0.0.1
extendedKeyUsage=serverAuth
keyUsage=digitalSignature,keyEncipherment
EOF

openssl x509 -req \
  -in backend-server.csr \
  -CA ca.crt \
  -CAkey ca.key \
  -CAcreateserial \
  -out backend-server.crt \
  -days 825 \
  -sha256 \
  -extfile backend-server.ext

openssl pkcs12 -export \
  -name backend-server \
  -inkey backend-server.key \
  -in backend-server.crt \
  -certfile ca.crt \
  -out backend-server.p12 \
  -passout "pass:$PASS"

# 3. Create Gateway client certificate.
openssl genrsa -out gateway-client.key 2048

openssl req -new \
  -key gateway-client.key \
  -subj "/CN=spring-security-api-gateway" \
  -out gateway-client.csr

cat > gateway-client.ext <<'EOF'
extendedKeyUsage=clientAuth
keyUsage=digitalSignature,keyEncipherment
EOF

openssl x509 -req \
  -in gateway-client.csr \
  -CA ca.crt \
  -CAkey ca.key \
  -CAcreateserial \
  -out gateway-client.crt \
  -days 825 \
  -sha256 \
  -extfile gateway-client.ext

openssl pkcs12 -export \
  -name gateway-client \
  -inkey gateway-client.key \
  -in gateway-client.crt \
  -certfile ca.crt \
  -out gateway-client.p12 \
  -passout "pass:$PASS"

# 4. Backend trusts the CA that issued the Gateway client certificate.
keytool -importcert -noprompt \
  -alias local-ca \
  -file ca.crt \
  -keystore backend-truststore.p12 \
  -storetype PKCS12 \
  -storepass "$PASS"

chmod 600 *.key *.p12

rm -f *.csr *.ext ca.srl

echo "Certificates generated in $OUT"