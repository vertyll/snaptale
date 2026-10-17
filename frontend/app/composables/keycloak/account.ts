import type { SecondFactor } from "~/utils/types";

const SIGN_IN_PATH = "/oauth2/authorization/keycloak";

/**
 * A page Keycloak shows to a signed-in person before returning: changing the password, setting up the second factor, or
 * removing a credential named by its id, which Keycloak asks to confirm.
 */
export type AccountAction = "UPDATE_PASSWORD" | "CONFIGURE_TOTP" | `delete_credential:${string}`;

/** Sends the browser to Keycloak's sign-in, or to one account page when an action is given. */
export function openKeycloak(action?: AccountAction) {
  return navigateTo(action ? `${SIGN_IN_PATH}?kc_action=${action}` : SIGN_IN_PATH, { external: true });
}

function changePassword() {
  return openKeycloak("UPDATE_PASSWORD");
}

/** Turns the second factor on when there is none, and off, after Keycloak asks to confirm, when there is one. */
function toggleSecondFactor(factor: SecondFactor) {
  return openKeycloak(factor.credentialId ? `delete_credential:${factor.credentialId}` : "CONFIGURE_TOTP");
}

/** The signed-in person's account on Keycloak: the password and the second factor, each on Keycloak's own pages. */
export function useKeycloakAccount() {
  const api = useApi();
  const secondFactor = ref<SecondFactor | null>(null);

  async function loadSecondFactor(): Promise<void> {
    secondFactor.value = await api.get<SecondFactor>("/api/me/second-factor").catch(() => null);
  }

  return { secondFactor, loadSecondFactor, changePassword, toggleSecondFactor };
}
