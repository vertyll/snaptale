<script setup lang="ts">
import type { Me } from "~/utils/types";

const { me } = useSession();
const { open } = useOverlays();
const requestFetch = useRequestFetch();
const { locale } = useLocale();

useHead({ htmlAttrs: { lang: locale } });

await useAsyncData("bootstrap", async () => {
  const session = await requestFetch<{ user: Me | null }>("/api/me");
  me.value = session.user;
  return true;
});

watch(open, (overlay) => document.body.classList.toggle("overflow-hidden", overlay !== null));
</script>

<template>
  <NuxtLayout>
    <NuxtPage />
  </NuxtLayout>
  <EditProfileOverlay v-if="open === 'editProfile'" />
</template>
