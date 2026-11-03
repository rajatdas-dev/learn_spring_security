package spring_security

import rego.v1

# Deny by default.
default allow := false

# ADMIN can access all Gateway-protected routes.
allow if {
    input.subject.role == "ADMIN"
}

# USER and MODERATOR can access user endpoints.
allow if {
    input.subject.role in {"USER", "MODERATOR"}
    startswith(input.request.path, "/user/")
}

# USER and MODERATOR can access document routes.
# Actual document ownership is checked by the backend.
allow if {
    input.subject.role in {"USER", "MODERATOR"}
    startswith(input.request.path, "/documents")
}