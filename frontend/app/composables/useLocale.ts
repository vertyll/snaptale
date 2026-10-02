export const LOCALES = ["pl", "en"] as const;
export type Locale = (typeof LOCALES)[number];

const ONE_YEAR = 60 * 60 * 24 * 365;

function supported(value: string | null | undefined): Locale {
  return LOCALES.find((locale) => locale === value) ?? "pl";
}

export function useLocale() {
  const cookie = useCookie<string>("lang", { default: () => "pl", maxAge: ONE_YEAR, sameSite: "lax" });
  const locale = computed<Locale>(() => supported(cookie.value));
  const { messages } = useMessages();

  async function setLocale(next: Locale) {
    messages.value = await $fetch<Record<string, string>>(`/api/messages?lang=${next}`);
    cookie.value = next;
  }

  return { locale, setLocale };
}
