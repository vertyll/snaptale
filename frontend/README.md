# SnapTale — front-end

A Nuxt application. It holds no token: sign-in and the session belong to the back-end
([Authentication](../docs/authentication.md)).

## Layout

| Path              | Holds                                                                   |
|-------------------|-------------------------------------------------------------------------|
| `app/pages`       | the routed pages                                                        |
| `app/components`  | the components the pages are built from                                 |
| `app/composables` | the API client (`useApi`), the session, the messages, forms and locale  |
| `app/utils`       | `ApiError`, formatting and shared types                                 |
| `server/routes`   | the proxy to the back-end: `/api`, `/oauth2`, `/login/oauth2`, `/media` |

`useApi` sends the CSRF token from the `XSRF-TOKEN` cookie in `X-XSRF-TOKEN` on every unsafe request.

## Text

Every label and every error is a key of the back-end's catalogue, formatted with `IntlMessageFormat` by `useMessages`
([Errors and translations](../docs/architecture.md#errors-and-translations)): `t(code, args)` for a label,
`errorText(error)` for a failed call, `ApiError.field(name)` next to a form's input. The language is the `lang`
cookie, which the back-end also passes to Keycloak.

## Running it

Start the infrastructure and the back-end first ([Development Setup](../docs/development-setup.md)), then:

```bash
npm install
npm run dev
```

The application is at `http://localhost:3000`. `.env.development` sets `NUXT_BACKEND_INTERNAL_URL`, the back-end the
server proxies to, and `NUXT_PUBLIC_KEYCLOAK_ACCOUNT_URL`, the Keycloak account page the profile links to.

## Checks

```bash
npm run format
npm run lint
npm run typecheck
```

## Production

The image takes `NUXT_BACKEND_INTERNAL_URL` and `NUXT_PUBLIC_KEYCLOAK_ACCOUNT_URL` from the environment.
