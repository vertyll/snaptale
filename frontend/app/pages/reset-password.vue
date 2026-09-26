<script setup lang="ts">
const { t } = useMessages();
const route = useRoute();
const api = useApi();
const { open } = useOverlays();
const { submit, pending, fieldError, formError } = useForm();
const token = computed(() => (typeof route.query.token === "string" ? route.query.token : ""));
const password = ref("");
const confirmPassword = ref("");
const done = ref(false);
const mismatch = computed(() => confirmPassword.value !== "" && confirmPassword.value !== password.value);

useHead(() => ({ title: t("resetPassword.pageTitle") }));

async function reset() {
  if (!mismatch.value) {
    done.value = await submit(() =>
      api.post("/api/auth/password/reset", { token: token.value, password: password.value })
    );
  }
}
</script>

<template>
  <main class="mx-auto w-full max-w-[470px] pt-[110px]">
    <h1 class="mb-6 text-center text-[28px] font-bold">{{ t("resetPassword.title") }}</h1>
    <div v-if="done" class="text-center" role="status">
      <p>{{ t("resetPassword.done") }}</p>
      <button type="button" class="text-brand mt-4 font-semibold" @click="open = 'login'">{{ t("nav.signIn") }}</button>
    </div>
    <p v-else-if="!token" class="text-center text-red-500">{{ t("resetPassword.invalidLink") }}</p>
    <form v-else class="px-6" @submit.prevent="reset">
      <TextField
        v-model="password"
        :label="t('resetPassword.password', { min: 8 })"
        type="password"
        autocomplete="new-password"
        :maxlength="72"
        autofocus
        :error="fieldError('password')"
        class="pb-2"
      />
      <TextField
        v-model="confirmPassword"
        :label="t('common.confirmPassword')"
        type="password"
        autocomplete="new-password"
        :maxlength="72"
        :error="mismatch ? t('common.passwordMismatch') : undefined"
      />
      <p v-if="formError" class="pt-2 font-semibold text-red-500" role="alert">{{ formError }}</p>
      <button
        type="submit"
        :disabled="!password || !confirmPassword || mismatch || pending"
        class="bg-brand mt-6 w-full rounded-sm py-3 text-[17px] font-semibold text-white disabled:bg-gray-200"
      >
        {{ t("resetPassword.submit") }}
      </button>
    </form>
  </main>
</template>
