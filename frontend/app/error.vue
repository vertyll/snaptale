<script setup lang="ts">
import type { NuxtError } from "#app";

const props = defineProps<{ error: NuxtError }>();
const notFound = computed(() => props.error.status === 404);
</script>

<template>
  <main class="flex min-h-screen flex-col items-center justify-center gap-4 text-center">
    <img width="115" src="~/assets/images/snaptale-logo.png" alt="SnapTale" />
    <h1 class="text-2xl font-bold">{{ notFound ? "Nie znaleziono strony" : "Coś poszło nie tak" }}</h1>
    <p class="text-gray-600">{{ notFound ? error.message : "Spróbuj ponownie za chwilę." }}</p>
    <button type="button" class="bg-brand rounded-md px-6 py-2 text-white" @click="clearError({ redirect: '/' })">
      Wróć na stronę główną
    </button>
  </main>
</template>
