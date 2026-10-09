# Architecture

## Two applications

```mermaid
flowchart LR
    browser([Browser])
    nuxt["frontend<br/>Nuxt"]
    back["backend<br/>Spring Boot"]
    kc[Keycloak]
    redis[("Redis<br/>sessions")]
    mysql[(MySQL)]
    media[("Media directory<br/>videos, avatars")]

    browser -- "SNAPTALE_SESSION cookie" --> nuxt
    nuxt -- "/api, /oauth2, /media forwarded" --> back
    back -- "sign-in, refresh, token keys" --> kc
    back --> redis
    back --> mysql
    back --> media
```

| Application                                     | What it is                                                      |
|-------------------------------------------------|-----------------------------------------------------------------|
| `frontend` ([front-end](../frontend/README.md)) | a Nuxt application: the pages and a server that proxies the API |
| `backend` ([back-end](../backend/README.md))    | a Spring Boot API over MySQL and Redis                          |

The browser talks only to the Nuxt server, which forwards `/api/*`, `/oauth2/*`, `/login/oauth2/*` and `/media/*` to the
back-end. The back-end's cookies are therefore set on the front-end's address and every API call is same-origin.

## Errors and translations

The back-end never sends a sentence a person reads. Every refusal is an RFC 9457 problem document
(`application/problem+json`, `common/ApiExceptionHandler`):

| Field    | Holds                                                                                        |
|----------|----------------------------------------------------------------------------------------------|
| `status` | the HTTP status                                                                              |
| `code`   | a key of the message catalogue; a refusal Spring raises itself gets `errors.status.{status}` |
| `args`   | the ICU arguments for that key                                                               |
| `errors` | in a validation error, one `{ code, args }` per invalid field                                |

The catalogue is the back-end's: it ships in `backend/src/main/resources/messages` (`pl.json`, `en.json`, ICU
MessageFormat) and `GET /api/messages?lang=pl|en` serves it, cached for an hour. It is not editable at runtime.

The front-end renders it. `useMessages` loads the catalogue for the language in the `lang` cookie and formats a key with
its arguments through `IntlMessageFormat`. A failed call is an `ApiError` (`utils/api-error.ts`): `summary` is the
problem's `{ code, args }`, `field(name)` the message for one input, and a response without a problem document becomes
`errors.status.{status}`. The same catalogue holds every label, so a new error or a new label is a new key in both
files, never a sentence in the code.
