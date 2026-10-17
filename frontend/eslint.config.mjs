import prettier from "eslint-config-prettier/flat";
import withNuxt from "./.nuxt/eslint.config.mjs";

export default withNuxt(
  {
    files: ["**/*.ts", "**/*.vue"],
    rules: {
      "@typescript-eslint/consistent-type-imports": "error",
      "@typescript-eslint/no-non-null-assertion": "error",
      eqeqeq: ["error", "always"],
      "vue/multi-word-component-names": "off",
      "no-restricted-imports": [
        "error",
        {
          patterns: [
            {
              group: ["~/composables/keycloak/*", "~~/app/composables/keycloak/*"],
              message: "Use the keycloak module through what its index exports.",
            },
          ],
        },
      ],
    },
  },
  prettier
);
