import { randomBytes } from "node:crypto";

/**
 * The policy of a page, with the nonce that lets the scripts Nuxt writes into it run, and no other inline script. Media
 * and avatars come through this server's `/media`, and every call through its `/api`, so everything else is 'self';
 * styles are inserted at run time by Vue and the cropper.
 */
function contentSecurityPolicy(nonce: string): string {
  return [
    "default-src 'self'",
    `script-src 'self' 'nonce-${nonce}'`,
    "style-src 'self' 'unsafe-inline'",
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

const SCRIPT_TAG = /<script\b/g;

/**
 * Sends a Content-Security-Policy with a nonce of its own for every page Nuxt renders, and gives that nonce to each
 * script in the page. Text a person wrote is escaped before it gets here, so every `<script` is one of Nuxt's tags. The
 * development server, whose scripts Vite writes, goes without.
 */
export default defineNitroPlugin((nitroApp) => {
  if (import.meta.dev) {
    return;
  }
  nitroApp.hooks.hook("render:html", (html, { event }) => {
    const nonce = randomBytes(16).toString("base64");
    const withNonce = (chunk: string) => chunk.replace(SCRIPT_TAG, `<script nonce="${nonce}"`);
    html.head = html.head.map(withNonce);
    html.bodyPrepend = html.bodyPrepend.map(withNonce);
    html.body = html.body.map(withNonce);
    html.bodyAppend = html.bodyAppend.map(withNonce);
    setResponseHeader(event, "Content-Security-Policy", contentSecurityPolicy(nonce));
  });
});
