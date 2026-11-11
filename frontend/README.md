# SnapTale — front-end

A Nuxt application. It holds no token: sign-in and the session belong to the back-end
([Authentication](../docs/authentication.md)).

## Layout

| Path                       | Holds                                                                                      |
|----------------------------|--------------------------------------------------------------------------------------------|
| `app/pages`                | the routed pages                                                                           |
| `app/components`           | the components the pages are built from                                                    |
| `app/composables`          | the API client (`useApi`), the session, the messages, forms and locale                     |
| `app/composables/keycloak` | Keycloak's pages: the shared language and the account actions; only `index.ts` is imported |
| `app/utils`                | `ApiError`, formatting and shared types                                                    |
| `server/routes`            | the proxy to the back-end: `/api`, `/oauth2`, `/login/oauth2`, `/media`                    |

`useApi` sends the CSRF token from the `XSRF-TOKEN` cookie in `X-XSRF-TOKEN` on every unsafe request.

## Text

Every label and every error is a key of the back-end's catalog, formatted with `IntlMessageFormat` by `useMessages`
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
server proxies to.

## Checks

```bash
npm run format
npm run lint
npm run typecheck
```

## Production

The image takes `NUXT_BACKEND_INTERNAL_URL` from the environment.

## Security headers

The security headers come from [`nuxt-security`](https://nuxt-security.vercel.app/), configured under `security` in
`nuxt.config.ts`. Each page carries a `Content-Security-Policy` with a nonce of its own, which the module puts on the
scripts and style elements Nuxt writes, and `'strict-dynamic'`, so only those scripts and the chunks they load run; no
other inline script, style element or style attribute applies, so a component sizes and styles itself with classes. The
module's rate and request size limits and its XSS validator are off: the back end limits and validates what reaches
it. The development server goes without the policy.
