import tailwindcss from "@tailwindcss/vite";

export default defineNuxtConfig({
  compatibilityDate: "2026-09-26",
  modules: ["@nuxt/eslint", "nuxt-security"],
  css: ["~/assets/css/main.css"],
  vite: { plugins: [tailwindcss()] },
  app: {
    head: {
      htmlAttrs: { lang: "pl" },
      title: "SnapTale",
      link: [{ rel: "icon", type: "image/x-icon", href: "/favicon.ico" }],
    },
  },
  security: {
    headers: {
      contentSecurityPolicy: {
        "default-src": ["'self'"],
        "script-src": ["'self'", "'nonce-{{nonce}}'", "'strict-dynamic'"],
        "script-src-attr": ["'none'"],
        "style-src": ["'self'", "'nonce-{{nonce}}'"],
        "img-src": ["'self'", "data:", "blob:"],
        "media-src": ["'self'", "blob:"],
        "font-src": ["'self'", "data:"],
        "connect-src": ["'self'"],
        "object-src": ["'none'"],
        "base-uri": ["'self'"],
        "form-action": ["'self'"],
        "frame-ancestors": ["'none'"],
        "upgrade-insecure-requests": false,
      },
      strictTransportSecurity: { maxAge: 31536000, includeSubdomains: true },
      xFrameOptions: "DENY",
      referrerPolicy: "strict-origin-when-cross-origin",
      permissionsPolicy: { camera: [], "display-capture": [], fullscreen: ["self"], geolocation: [], microphone: [] },
    },
    // The back end limits and validates the requests passed to it; these would also refuse its uploads of up to 100 MB.
    rateLimiter: false,
    requestSizeLimiter: false,
    xssValidator: false,
    corsHandler: false,
    removeLoggers: false,
  },
  $development: {
    // Vite's development server injects styles without a nonce.
    security: { headers: { contentSecurityPolicy: false } },
  },
  // A module folder exposes only its index; its other files are its own.
  imports: { dirs: ["composables/keycloak/index.ts"] },
  runtimeConfig: {
    backendInternalUrl: "",
  },
  typescript: { strict: true, typeCheck: false },
});
