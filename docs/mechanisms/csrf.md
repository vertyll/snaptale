# CSRF

Why a forged request from another site cannot act with the user's session cookie.

The session is a cookie, so every unsafe request needs the CSRF token: the back-end sets it in the `XSRF-TOKEN` cookie
and the front-end sends it back in `X-XSRF-TOKEN`. A request carrying `Authorization` is exempt, because no cookie is
involved.
