<p align="center">
    <img alt="" src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white">
    <img alt="" src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white">
    <img alt="" src="https://img.shields.io/badge/Nuxt-00DC82?style=for-the-badge&logo=nuxt&logoColor=white">
    <img alt="" src="https://img.shields.io/badge/Vue.js-4FC08D?style=for-the-badge&logo=vuedotjs&logoColor=white">
    <img alt="" src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
    <img alt="" src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white">
    <img alt="" src="https://img.shields.io/badge/Keycloak-00b8e3?style=for-the-badge&logo=keycloak&logoColor=4D4D4D">
    <img alt="" src="https://img.shields.io/badge/Tailwind_CSS-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white">
    <img alt="" src="https://img.shields.io/badge/Apache_Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white">
</p>

## Project Assumptions

Short video sharing application.

## Link: https://snaptale.vertyll.dev

## Technology Stack

### Back-end:

- Spring Boot.
- Java.
- Maven.
- MySQL.
- Redis.
- Flyway.
- JUnit.
- Testcontainers.
- Lombok.
- Spring Security.
- Spring Data JPA.
- Spring Web.
- Spring Session (Redis).
- Spring Mail.

### Front-end:

- Nuxt.
- Vue.js.
- TypeScript.
- Tailwind CSS.

### Authentication:

- **Identity provider**: Keycloak (realm `snaptale`) owns every page that touches a credential: sign-up, sign-in, email
  verification, password reset, two-factor authentication and acceptance of the terms of use. The application never sees
  a password.
- **Pattern**: BFF with Spring Security's OAuth2 client: the authorization code flow and PKCE, tokens kept by the
  back-end, and only the `SNAPTALE_SESSION` cookie (`HttpOnly`, `SameSite=Lax`, `Secure` in production) with a CSRF
  token in the browser.
- **Session store**: Redis (Spring Session, `snaptale:session` namespace).
- **JWT**: on every API request the back-end takes the access token from the session, refreshing it when it is about to
  expire, and verifies it like a resource server: signature (Keycloak's JWKS), issuer, expiry and audience
  (`snaptale-api`). The request's identity comes from that token, not from the session.
- **State**: the back-end is stateless: every request is authorized by the JWT alone, so any instance can serve it. The
  only state is the browser session holding the tokens, and it lives in Redis, outside the application.
- **Token lifecycle**: access tokens live five minutes; every refresh returns a new refresh token and invalidates the
  old one, and concurrent requests of one session share a single refresh. A refresh Keycloak refuses ends the session,
  so a blocked account or a revoked role stops working within minutes. Signing out also ends the Keycloak session.
- **Accounts**: the SnapTale account (profile, videos, follows) is created in MySQL at the first sign-in and linked to
  the Keycloak user; its email follows Keycloak on every sign-in.

### Core back-end:

- Maven build system.
- The application has an exception handling mechanism (RFC 9457 problem details with message codes).
- The application has a logging mechanism.
- The application has separate environments for local and prod.
- The application has a dedicated configuration file.
- The application has Flyway database migration mechanism.
- The application has ICU messages in Polish and English.
- And many other features that can be found in the application code.

### Core front-end:

- The front-end proxies `/api`, `/media` and the sign-in endpoints to the back-end.
- The application has separate environments for local and prod.
- Polish and English.
- And many other features that can be found in the application code.

### Other:

- Docker for development environment.
- PMD for static code analysis.
- SpotBugs for static code analysis.
- JSpecify for null-safety annotations.
- NullAway for null-safety checks.
- Error Prone for static code analysis.
- Spotless for code formatting.
- ESLint and Prettier for the front-end.

## Preview Screenshots

![Project View](https://raw.githubusercontent.com/vertyll/SnapTale/main/screenshots/snaptale4.png)
![Project View](https://raw.githubusercontent.com/vertyll/SnapTale/main/screenshots/snaptale2.png)
![Project View](https://raw.githubusercontent.com/vertyll/SnapTale/main/screenshots/snaptale5.png)
![Project View](https://raw.githubusercontent.com/vertyll/SnapTale/main/screenshots/snaptale1.png)
![Project View](https://raw.githubusercontent.com/vertyll/SnapTale/main/screenshots/snaptale3.png)
