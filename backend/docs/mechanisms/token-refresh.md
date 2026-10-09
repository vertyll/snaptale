# Token refresh

How the session keeps a valid access token without signing the user out when requests race.

The session lives in Redis (Spring Session, namespace `snaptale:session`) and lasts ten hours. Access tokens live five
minutes.

> [!IMPORTANT]
>
> Keycloak rotates refresh tokens: every refresh returns a new one and invalidates the old one, and replaying a spent
> one ends the session. Two requests of one session refreshing at once would therefore sign the user out.

A refresh therefore runs once per refresh token:

- within one instance, `SingleFlightRefreshTokenProvider` lets the first request refresh and hands its result to the
  others;
- across instances, `SharedRefreshes` takes a lock in Redis; the instance holding it refreshes and leaves the new tokens
  in Redis for 30 seconds, where the others pick them up.

When Keycloak refuses a refresh (`invalid_grant`), the session is invalidated and the request goes on anonymous: a
blocked account stops working within minutes.
