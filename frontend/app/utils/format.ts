const DATE = new Intl.DateTimeFormat("pl-PL", { day: "numeric", month: "short", year: "numeric" });

export function formatDate(iso: string): string {
  return DATE.format(new Date(iso));
}

export function handleOf(name: string): string {
  return name.replaceAll(/\s+/g, "").toLowerCase();
}
