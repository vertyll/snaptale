import tailwindcss from "@tailwindcss/vite";

export default defineNuxtConfig({
  compatibilityDate: "2026-09-26",
  modules: ["@nuxt/icon", "@nuxt/eslint"],
  css: ["~/assets/css/main.css"],
  vite: { plugins: [tailwindcss()] },
  app: {
    head: {
      htmlAttrs: { lang: "pl" },
      title: "SnapTale",
      meta: [{ name: "description", content: "SnapTale - krótkie filmy od ludzi, których lubisz." }],
      link: [{ rel: "icon", type: "image/x-icon", href: "/favicon.ico" }],
    },
  },
  icon: { serverBundle: { collections: ["mdi"] } },
  runtimeConfig: {
    backendInternalUrl: "",
  },
  typescript: { strict: true, typeCheck: false },
});
