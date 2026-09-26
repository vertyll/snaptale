<script setup lang="ts">
const { t } = useMessages();
const route = useRoute();
const api = useApi();
const { isSignedIn, refresh } = useSession();
const { submit, formError } = useForm();
const state = ref<"verifying" | "verified" | "failed">("verifying");

useHead(() => ({ title: t("verifyEmail.pageTitle") }));

onMounted(async () => {
  const token = typeof route.query.token === "string" ? route.query.token : "";
  const ok = await submit(async () => {
    await api.post("/api/auth/email/verify", { token });
    if (isSignedIn.value) {
      await refresh();
    }
  });
  state.value = ok ? "verified" : "failed";
});
</script>

<template>
  <main class="mx-auto w-full max-w-[470px] pt-[110px] text-center">
    <h1 class="mb-6 text-[28px] font-bold">{{ t("verifyEmail.title") }}</h1>
    <Icon v-if="state === 'verifying'" name="mdi:loading" size="48" class="text-brand animate-spin" />
    <p v-else-if="state === 'verified'" role="status">{{ t("verifyEmail.done") }}</p>
    <p v-else class="font-semibold text-red-500" role="alert">{{ formError }}</p>
    <NuxtLink to="/" class="text-brand mt-6 inline-block font-semibold">{{ t("verifyEmail.continue") }}</NuxtLink>
  </main>
</template>
