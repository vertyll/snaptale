const SIGN_IN_PATH = "/oauth2/authorization/keycloak";

/**
 * The sign-in, which returns to `returnTo`, a path on this site, once the person is signed in, and asks Keycloak for one
 * account page first when `action` names one.
 */
export function signInUrl(returnTo: string, action?: string): string {
  const query = new URLSearchParams({ return_to: returnTo });
  if (action) {
    query.set("kc_action", action);
  }
  return `${SIGN_IN_PATH}?${query}`;
}
