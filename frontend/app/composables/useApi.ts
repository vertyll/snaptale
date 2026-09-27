import { ApiError } from "~/utils/api-error";
import type { ProblemDetail } from "~/utils/types";

type Method = "GET" | "POST" | "PUT" | "PATCH" | "DELETE";

const CSRF_COOKIE = "XSRF-TOKEN";
const CSRF_HEADER = "X-XSRF-TOKEN";

async function csrfToken(): Promise<string> {
  const existing = readCookie(CSRF_COOKIE);
  if (existing) {
    return existing;
  }
  await $fetch("/api/me");
  const token = readCookie(CSRF_COOKIE);
  if (!token) {
    throw new Error("Back-end did not set the CSRF cookie");
  }
  return token;
}

async function call<T>(method: Method, path: string, body?: BodyInit | Record<string, unknown>): Promise<T> {
  const headers: Record<string, string> = method === "GET" ? {} : { [CSRF_HEADER]: await csrfToken() };
  try {
    return await $fetch<T>(path, { method, body, headers, credentials: "same-origin" });
  } catch (error: unknown) {
    throw toApiError(error);
  }
}

export function useApi() {
  return {
    get: <T>(path: string) => call<T>("GET", path),
    post: <T>(path: string, body?: Record<string, unknown> | FormData) => call<T>("POST", path, body),
    put: <T>(path: string, body?: Record<string, unknown> | FormData) => call<T>("PUT", path, body),
    patch: <T>(path: string, body: Record<string, unknown>) => call<T>("PATCH", path, body),
    del: <T>(path: string) => call<T>("DELETE", path),
  };
}

function toApiError(error: unknown): ApiError {
  const fetchError = error as { status?: number; statusCode?: number; data?: unknown };
  const status = fetchError.status ?? fetchError.statusCode ?? 0;
  const data = fetchError.data;
  const problem = data && typeof data === "object" && "code" in data ? (data as ProblemDetail) : null;
  return new ApiError(status, problem);
}

function readCookie(name: string): string | null {
  const prefix = `${name}=`;
  const entry = document.cookie.split("; ").find((cookie) => cookie.startsWith(prefix));
  return entry ? decodeURIComponent(entry.slice(prefix.length)) : null;
}
