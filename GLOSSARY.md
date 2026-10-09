# Glossary

Every term the documentation uses without defining it on the spot, and where it is explained. The specifications behind
them are in [STANDARDS.md](STANDARDS.md).

| Term | Meaning | Explained in |
|---|---|---|
| Access token | Short-lived JWT (five minutes) that authorizes one request. | [Authentication: Every API request is authorized by a token](docs/authentication.md#every-api-request-is-authorized-by-a-token) |
| Audience | The `aud` claim naming whom a token is for; a token for anyone else is refused. | [Authentication: Every API request is authorized by a token](docs/authentication.md#every-api-request-is-authorized-by-a-token) |
| Authorization code flow | Sign-in by redirecting to Keycloak and exchanging the code it returns, on the server. | [Authentication: Signing in](docs/authentication.md#signing-in) |
| Avatar | A profile picture, cropped to 512 px and stored in the media directory. | [SnapTale — back-end: Media](backend/README.md#media) |
| BFF (backend for frontend) | The back-end signs the user in and keeps the tokens; the browser holds only a session cookie. | [Authentication](docs/authentication.md) |
| Catalog | Every UI message, in Polish and English, shipped with the back-end. | [Architecture: Errors and translations](docs/architecture.md#errors-and-translations) |
| CSRF token | The `XSRF-TOKEN` cookie value every unsafe request repeats in `X-XSRF-TOKEN`. | [Authentication: CSRF](docs/authentication.md#csrf) |
| Feed | The paged list of posts the front page shows. | [SnapTale — back-end: Layout](backend/README.md#layout) |
| Follow | One user subscribing to another's posts. | [SnapTale — back-end: Layout](backend/README.md#layout) |
| Keycloak realm | The Keycloak tenant holding this application's users, roles and clients. | [Authentication](docs/authentication.md) |
| Media directory | Where videos and avatars are stored, served from `/media`. | [SnapTale — back-end: Media](backend/README.md#media) |
| Message key | A key of the catalog the back-end sends instead of a sentence; the front-end renders it. | [Architecture: Errors and translations](docs/architecture.md#errors-and-translations) |
| PKCE | A one-time secret binding the returned code to the browser that started the sign-in. | [Authentication: Signing in](docs/authentication.md#signing-in) |
| Post | A short video with its likes and comments. | [SnapTale — back-end: Layout](backend/README.md#layout) |
| Problem document | The JSON body of every refusal, carrying a message key and its arguments. | [Architecture: Errors and translations](docs/architecture.md#errors-and-translations) |
| Proxy | The Nuxt server forwarding `/api`, `/oauth2`, `/login/oauth2` and `/media` to the back-end. | [Architecture: Two applications](docs/architecture.md#two-applications) |
| Refresh token | Long-lived token traded for a new access token; Keycloak rotates it on every use. | [Authentication: Sessions and refreshing](docs/authentication.md#sessions-and-refreshing) |
| Refresh token rotation | Every refresh invalidates the refresh token it used; replaying a spent one ends the session. | [Authentication: Sessions and refreshing](docs/authentication.md#sessions-and-refreshing) |
| Session | `SNAPTALE_SESSION`, kept in Redis for ten hours, holding the tokens. | [Authentication: Sessions and refreshing](docs/authentication.md#sessions-and-refreshing) |
| Sign-out | Ends the local session and the Keycloak session. | [Authentication: Signing out](docs/authentication.md#signing-out) |
| Single-flight refresh | One refresh per refresh token, shared by every request of the session that needs it at once. | [Authentication: Sessions and refreshing](docs/authentication.md#sessions-and-refreshing) |
| SnapTale account | The profile, videos and follows, linked to a Keycloak identity at the first sign-in. | [SnapTale — back-end: Accounts](backend/README.md#accounts) |
