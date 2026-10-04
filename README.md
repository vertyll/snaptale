## Project Assumptions

Short video sharing application.

## Link: https://snaptale.vertyll.dev

## Technology Stack

### Back-end:

- Spring Boot.
- Java.
- Maven.
- MySQL.
- Flyway.
- JUnit.
- Testcontainers.
- Lombok.
- Spring Security.
- Spring Data JPA.
- Spring Web.
- Spring Session (JDBC).
- Spring Mail.

### Front-end:

- Nuxt.
- Vue.js.
- TypeScript.
- Tailwind CSS.

### Authentication:

- Keycloak (realm `snaptale`) handles sign-up, sign-in, email verification, password reset, two-factor authentication
  and acceptance of the terms of use.
- The back-end signs users in with the authorization code flow and PKCE, keeps the tokens in its session, stored in
  MySQL, and gives the browser only the `SNAPTALE_SESSION` cookie (`HttpOnly`, `SameSite=Lax`, `Secure` in production)
  with a CSRF token.
- The SnapTale account (profile, videos, follows) is created at the first sign-in and linked to the Keycloak user.
- Locally, `docker-compose.local.yml` runs MySQL, Keycloak on `:9000` (admin/admin) with the realm from
  `keycloak/realm-export.json`, and maildev.

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
