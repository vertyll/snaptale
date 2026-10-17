export const LOCALES = ["pl", "en"] as const;
export type Locale = (typeof LOCALES)[number];

const ONE_YEAR = 60 * 60 * 24 * 365;

function find(value: string | null | undefined): Locale | null {
  return LOCALES.find((locale) => locale === value) ?? null;
}

export function useLocale() {
  const cookie = useCookie<string>("lang", { default: () => "pl", maxAge: ONE_YEAR, sameSite: "lax" });
  const keycloak = useKeycloakPreferences();
  const locale = computed<Locale>(() => find(keycloak.chosenLanguage.value) ?? find(cookie.value) ?? "pl");
  // The back-end reads `lang`, for the language of Keycloak's pages.
  if (cookie.value !== locale.value) {
    cookie.value = locale.value;
  }
  const { messages } = useMessages();

  async function setLocale(next: Locale) {
    messages.value = await $fetch<Record<string, string>>(`/api/messages?lang=${next}`);
    cookie.value = next;
    await keycloak.languageChosen(next);
  }

  return { locale, setLocale };
}
