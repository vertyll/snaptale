<script setup lang="ts">
import type { Me } from "~/utils/types";

const { me } = useSession();
const { messages } = useMessages();
const { open } = useOverlays();
const requestFetch = useRequestFetch();

await useAsyncData("bootstrap", async () => {
  const [session, texts] = await Promise.all([
    requestFetch<{ user: Me | null }>("/api/me"),
    requestFetch<Record<string, string>>("/api/messages"),
  ]);
  me.value = session.user;
  messages.value = texts;
  return true;
});

watch(open, (overlay) => document.body.classList.toggle("overflow-hidden", overlay !== null));
</script>

<template>
  <NuxtLayout>
    <NuxtPage />
  </NuxtLayout>
  <AuthOverlay v-if="open === 'login' || open === 'register' || open === 'forgotPassword'" />
  <EditProfileOverlay v-if="open === 'editProfile'" />
</template>
