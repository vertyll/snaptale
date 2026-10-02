import type { H3Event } from "h3";

export function backendUrl(path: string, backendInternalUrl: string): string {
  if (!backendInternalUrl) {
    throw new Error("Missing NUXT_BACKEND_INTERNAL_URL (internal back-end URL)");
  }
  return new URL(path, backendInternalUrl).toString();
}

export function proxyToBackend(event: H3Event) {
  return proxyRequest(event, backendUrl(event.path, useRuntimeConfig(event).backendInternalUrl), {
    fetchOptions: { redirect: "manual" },
  });
}
