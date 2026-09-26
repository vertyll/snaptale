import type { Me } from "~/utils/types";

export function useSession() {
  const me = useState<Me | null>("me", () => null);
  const api = useApi();

  async function refresh(): Promise<void> {
    me.value = (await api.get<{ user: Me | null }>("/api/me")).user;
  }

  async function logout(): Promise<void> {
    await api.post("/api/auth/logout");
    me.value = null;
  }

  return { me, isSignedIn: computed(() => me.value !== null), refresh, logout };
}
