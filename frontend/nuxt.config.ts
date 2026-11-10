import tailwindcss from "@tailwindcss/vite";

export default defineNuxtConfig({
  compatibilityDate: "2026-09-26",
  modules: ["@nuxt/eslint"],
  css: ["~/assets/css/main.css"],
  vite: { plugins: [tailwindcss()] },
  app: {
    head: {
      htmlAttrs: { lang: "pl" },
      title: "SnapTale",
      link: [{ rel: "icon", type: "image/x-icon", href: "/favicon.ico" }],
    },
  },
  // The Content-Security-Policy, which needs a nonce for each page, is sent by server/plugins.
  routeRules: {
    "/**": {
      headers: {
        "Strict-Transport-Security": "max-age=31536000; includeSubDomains",
        "X-Content-Type-Options": "nosniff",
        "X-Frame-Options": "DENY",
        "Referrer-Policy": "strict-origin-when-cross-origin",
      },
    },
  },
  // A module folder exposes only its index; its other files are its own.
  imports: { dirs: ["composables/keycloak/index.ts"] },
  runtimeConfig: {
    backendInternalUrl: "",
  },
  typescript: { strict: true, typeCheck: false },
});
