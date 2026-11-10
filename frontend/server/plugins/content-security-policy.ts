import { randomBytes } from "node:crypto";

/**
 * The policy of a page, with the nonce that lets the scripts and style elements Nuxt writes into it apply, and no
 * other inline script, style element or style attribute. Media and avatars come through this server's `/media`, and
 * every call through its `/api`, so everything else is 'self'.
 */
function contentSecurityPolicy(nonce: string): string {
  return [
    "default-src 'self'",
    `script-src 'self' 'nonce-${nonce}'`,
    `style-src 'self' 'nonce-${nonce}'`,
    "img-src 'self' data: blob:",
    "media-src 'self' blob:",
    "font-src 'self' data:",
    "connect-src 'self'",
    "object-src 'none'",
    "base-uri 'self'",
    "form-action 'self'",
    "frame-ancestors 'none'",
  ].join("; ");
}

const INLINE_TAG = /<(script|style)\b/g;

/**
 * Sends a Content-Security-Policy with a nonce of its own for every page Nuxt renders, and gives that nonce to each
 * script and style element in the page. Text a person wrote is escaped before it gets here, so every `<script` and
 * `<style` is one of Nuxt's tags. The development server, whose scripts and styles Vite writes, goes without.
 */
export default defineNitroPlugin((nitroApp) => {
  if (import.meta.dev) {
    return;
  }
  nitroApp.hooks.hook("render:html", (html, { event }) => {
    const nonce = randomBytes(16).toString("base64");
    const withNonce = (chunk: string) => chunk.replace(INLINE_TAG, `<$1 nonce="${nonce}"`);
    html.head = html.head.map(withNonce);
    html.bodyPrepend = html.bodyPrepend.map(withNonce);
    html.body = html.body.map(withNonce);
    html.bodyAppend = html.bodyAppend.map(withNonce);
    setResponseHeader(event, "Content-Security-Policy", contentSecurityPolicy(nonce));
  });
});
