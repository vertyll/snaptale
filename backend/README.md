# SnapTale — back-end

A Spring Boot API over MySQL and Redis. It signs users in and keeps their tokens
([Authentication](../docs/authentication.md)) and answers errors as message keys
([Errors and translations](../docs/architecture.md#errors-and-translations)).

## Layout

| Package    | Holds                                                    |
|------------|----------------------------------------------------------|
| `post`     | videos, the feed, likes and comments                     |
| `follow`   | who follows whom                                         |
| `profile`  | a user's public profile                                  |
| `user`     | the SnapTale account, the signed-in user and suggestions |
| `auth`     | linking a Keycloak identity to a SnapTale account        |
| `security` | sign-in, sessions and tokens                             |
| `media`    | storing videos and avatars on disk and serving them      |
| `messages` | the UI messages in Polish and English                    |
| `common`   | errors, validation limits and shared types               |

## Access

`security/SecurityConfig` decides by path: the public reads are listed in `PUBLIC_READ_ENDPOINTS`, every other
`/api/**` call needs a signed-in user, and anything else is refused (`denyAll()`), so a new endpoint is closed until it
is listed there.

## Accounts

The SnapTale account holds what Keycloak does not: the profile, the videos, the follows. It lives in MySQL and is linked
to the Keycloak identifier. `UserAccounts.signIn` creates it at the first sign-in, or the first call with a bearer
token, taking the display name from Keycloak; afterwards the email follows Keycloak on every sign-in, while the name is
the user's own to change.

## Messages

The UI messages ship in `src/main/resources/messages`, ICU MessageFormat, and are served by `MessagesController`.
A new error is a new key in both files and a constant in `common/MessageKeys`.

## Database

MySQL, migrated by Flyway (`backend/src/main/resources/db/migration`); Hibernate only validates the schema.

## Mechanisms

- [Media storage](docs/mechanisms/media-storage.md) – Where uploaded files are written, and how they reach the browser.
- [Token refresh](docs/mechanisms/token-refresh.md) – How the session keeps a valid access token without signing the
  user out when requests race.

## Running it

Start the infrastructure first ([Development Setup](../docs/development-setup.md)), then:

```bash
./mvnw spring-boot:run
```

The `local` profile is the default and points at the local containers; it listens on `http://localhost:8080`. Uploaded
videos and avatars go to `media/`.

## Checks

```bash
./mvnw spotless:apply   # format
./mvnw verify           # Spotless, PMD, SpotBugs and the tests
```

The tests start MySQL and Redis with Testcontainers, so Docker or Podman must be running.

## Production

The `prod` profile, which takes its settings from the environment:

| Variable                                                                | Purpose                                                            |
|-------------------------------------------------------------------------|--------------------------------------------------------------------|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`                                  | MySQL                                                              |
| `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`                            | Redis, over TLS                                                    |
| `TRUSTSTORE_PASSWORD`                                                   | the truststore at `/tls-store/truststore.p12`, for MySQL and Redis |
| `KEYCLOAK_REALM_URL`, `KEYCLOAK_CLIENT_SECRET`                          | the realm and the confidential client                              |
| `FRONTEND_URL`                                                          | the front-end's public address                                     |
| `MEDIA_DIRECTORY`                                                       | where videos and avatars are stored; a persistent volume           |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | SMTP                                                               |
