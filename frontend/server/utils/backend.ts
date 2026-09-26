export function backendUrl(path: string, backendInternalUrl: string): string {
  if (!backendInternalUrl) {
    throw new Error("Missing NUXT_BACKEND_INTERNAL_URL (internal back-end URL)");
  }
  return new URL(path, backendInternalUrl).toString();
}
