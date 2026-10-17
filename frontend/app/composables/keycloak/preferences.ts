const ONE_YEAR = 60 * 60 * 24 * 365;

/**
 * The language the reader last chose, in SnapTale or on Keycloak's pages, which both write: a shared record rather than
 * a message, so the two agree however the reader moves between them. It lives under the domain SnapTale shares with
 * Keycloak, the parent of this host; on localhost and an IP address every port already shares cookies. Its name ends in
 * the realm's, as Keycloak's theme builds it: `realm` in `keycloak/realm-export.json`.
 */
const SHARED_LOCALE_COOKIE = "ui_locale_snaptale";

function sharedDomain(hostname: string): string | undefined {
  const labels = hostname.split(".");
  const isAddress = labels.every((label) => /^\d+$/.test(label));
  return labels.length > 2 && !isAddress ? labels.slice(1).join(".") : undefined;
}

/** What SnapTale shares with Keycloak's pages: the reader's language, both ways. */
export function useKeycloakPreferences() {
  const url = useRequestURL();
  const shared = useCookie<string | null>(SHARED_LOCALE_COOKIE, {
    default: () => null,
    maxAge: ONE_YEAR,
    sameSite: "lax",
    domain: sharedDomain(url.hostname),
    secure: url.protocol === "https:",
  });
  const { me } = useSession();
  const api = useApi();

  /** The language last chosen here or on Keycloak's pages, when one was. */
  const chosenLanguage = computed(() => shared.value);

  /**
   * Records a language chosen here, for Keycloak's pages: in the shared cookie, and on the signed-in person's account,
   * which Keycloak shows a signed-in person in.
   */
  async function languageChosen(language: string): Promise<void> {
    shared.value = language;
    if (me.value) {
      await api.put("/api/me/language", { language }).catch(() => undefined);
    }
  }

  return { chosenLanguage, languageChosen };
}
