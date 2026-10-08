# Authentication

- **Identity provider**: Keycloak (realm `snaptale`) owns every page that touches a credential: sign-up, sign-in, email
  verification, password reset, two-factor authentication and acceptance of the terms of use. The application never sees
  a password.
- **Pattern**: BFF with Spring Security's OAuth2 client: the authorization code flow and PKCE, tokens kept by the
  back-end, and only the `SNAPTALE_SESSION` cookie (`HttpOnly`, `SameSite=Lax`, `Secure` in production) with a CSRF
  token in the browser.
- **Session store**: Redis (Spring Session, `snaptale:session` namespace).
- **JWT**: on every API request the back-end takes the access token from the session, refreshing it when it is about to
  expire, and verifies it like a resource server: signature (Keycloak's JWKS), issuer, expiry and audience
  (`snaptale-api`). The request's identity comes from that token, not from the session. A client without a browser calls
  the same API with `Authorization: Bearer` and its own Keycloak token: the same checks apply, its account is linked
  like at a sign-in, and no CSRF token is needed, since no cookie is involved.
- **State**: the back-end is stateless: every request is authorized by the JWT alone, so any instance can serve it. The
  only state is the browser session holding the tokens, and it lives in Redis, outside the application.
- **Token lifecycle**: access tokens live five minutes; every refresh returns a new refresh token and invalidates the
  old one, and concurrent requests of one session share a single refresh, across replicas too (a lock in Redis). A
  refresh Keycloak refuses ends the session, so a blocked account or a revoked role stops working within minutes.
  Signing out also ends the Keycloak session.
- **Accounts**: the SnapTale account (profile, videos, follows) is created in MySQL at the first sign-in and linked to
  the Keycloak user; its email follows Keycloak on every sign-in.
