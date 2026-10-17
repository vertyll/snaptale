import type { Me } from "~/utils/types";

const LOGIN_PATH = "/oauth2/authorization/keycloak";

/**
 * A page Keycloak shows to a signed-in person before returning: changing the password, setting up the second factor, or
 * removing a credential named by its id, which Keycloak asks to confirm.
 */
export type AccountAction = "UPDATE_PASSWORD" | "CONFIGURE_TOTP" | `delete_credential:${string}`;

function signIn() {
  return navigateTo(LOGIN_PATH, { external: true });
}

function startAccountAction(action: AccountAction) {
  return navigateTo(`${LOGIN_PATH}?kc_action=${action}`, { external: true });
}

export function useSession() {
  const me = useState<Me | null>("me", () => null);
  const api = useApi();

  async function refresh(): Promise<void> {
    me.value = (await api.get<{ user: Me | null }>("/api/me")).user;
  }

  async function logout(): Promise<void> {
    const { logoutUrl } = await api.post<{ logoutUrl: string }>("/api/auth/logout");
    me.value = null;
    await navigateTo(logoutUrl, { external: true });
  }

  return { me, isSignedIn: computed(() => me.value !== null), refresh, signIn, startAccountAction, logout };
}
