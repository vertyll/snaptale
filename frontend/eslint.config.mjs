import stylistic from "@stylistic/eslint-plugin";
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
      "no-restricted-syntax": [
        "error",
        {
          // The same boundary for a dynamic import, which no-restricted-imports does not see.
          selector: "ImportExpression[source.value=/(^|\\/)keycloak\\/(?!index(\\.ts)?$).+/]",
          message: "Use the keycloak module through what its index exports.",
        },
      ],
      "no-restricted-imports": [
        "error",
        {
          patterns: [
            {
              // Any path into the module's folder but its index, whatever alias or relative path reaches it.
              regex: "(^|/)keycloak/(?!index(\\.ts)?$).+",
              message: "Use the keycloak module through what its index exports.",
            },
          ],
        },
      ],
    },
  },
  prettier,
  {
    // Comments only, as Prettier keeps code to its width but never wraps a comment; last, as the Prettier config
    // turns line length off.
    plugins: { "@stylistic": stylistic },
    rules: {
      "@stylistic/max-len": ["error", { code: Number.MAX_SAFE_INTEGER, comments: 120, ignoreUrls: true }],
    },
  }
);
