
#!/usr/bin/env bash
set -euo pipefail

# Generate ES256 (ECDSA P-256) JWT key pair.

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JWT_DIR="$PROJECT_ROOT/secrets/jwt"

mkdir -p "$JWT_DIR"

# Generate EC private key in PKCS#8 format.
openssl ecparam -name prime256v1 -genkey -noout \
  | openssl pkcs8 -topk8 -nocrypt \
      -out "$JWT_DIR/private_key.pem"

# Extract public key.
openssl pkey \
  -in "$JWT_DIR/private_key.pem" \
  -pubout \
  -out "$JWT_DIR/public_key.pem"

chmod 600 "$JWT_DIR/private_key.pem"

echo "JWT ES256 keys generated successfully:"
echo "$JWT_DIR/private_key.pem"
echo "$JWT_DIR/public_key.pem"
