export function formatDate(iso: string, locale: string): string {
  return new Intl.DateTimeFormat(locale, { day: "numeric", month: "short", year: "numeric" }).format(new Date(iso));
}

export function handleOf(name: string): string {
  return name.replaceAll(/\s+/g, "").toLowerCase();
}
