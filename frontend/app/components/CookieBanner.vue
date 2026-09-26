<script setup lang="ts">
const accepted = useCookie<string | null>("cookies_accepted", { maxAge: 60 * 60 * 24 * 365, sameSite: "lax" });
</script>

<template>
  <div
    v-if="!accepted"
    class="fixed right-0 bottom-0 left-0 z-50 border-t border-gray-200 bg-white p-4 shadow-md"
    role="region"
    aria-label="Informacja o plikach cookie"
  >
    <div class="container mx-auto flex flex-col items-center justify-between sm:flex-row">
      <p class="mb-2 text-sm text-gray-700 sm:mb-0">
        Ta strona używa plików cookie, aby poprawić Twoje doświadczenie. Korzystając z naszej strony, zgadzasz się na
        wykorzystanie plików cookie.
      </p>
      <button type="button" class="bg-brand rounded px-4 py-2 text-white" @click="accepted = 'true'">Akceptuję</button>
    </div>
  </div>
</template>
