<script setup lang="ts">
import type { NuxtError } from "#app";

const { t } = useMessages();
const props = defineProps<{ error: NuxtError }>();
const notFound = computed(() => props.error.status === 404);
const detail = computed(() => {
  const data = props.error.data as { message?: string } | undefined;
  return data?.message ?? t("error.notFoundDetail");
});
</script>

<template>
  <main class="flex min-h-screen flex-col items-center justify-center gap-4 text-center">
    <img width="115" src="~/assets/images/snaptale-logo.png" alt="SnapTale" />
    <h1 class="text-2xl font-bold">{{ notFound ? t("error.notFound") : t("error.generic") }}</h1>
    <p class="text-gray-600">{{ notFound ? detail : t("error.retry") }}</p>
    <button type="button" class="bg-brand rounded-md px-6 py-2 text-white" @click="clearError({ redirect: '/' })">
      {{ t("error.home") }}
    </button>
  </main>
</template>
