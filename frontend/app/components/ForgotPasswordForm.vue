<script setup lang="ts">
const { t } = useMessages();
const api = useApi();
const { submit, pending, fieldError, formError } = useForm();
const email = ref("");
const sent = ref(false);

async function send() {
  sent.value = await submit(() => api.post("/api/auth/password/forgot", { email: email.value }));
}
</script>

<template>
  <form class="px-6" @submit.prevent="send">
    <h2 class="mb-4 text-center text-[28px] font-bold">{{ t("auth.forgot.title") }}</h2>
    <output v-if="sent" class="block text-center text-gray-700">
      {{ t("auth.forgot.sent", { email }) }}
    </output>
    <template v-else>
      <p class="pb-4 text-sm text-gray-600">{{ t("auth.forgot.description") }}</p>
      <TextField
        v-model="email"
        :label="t('common.email')"
        type="email"
        autocomplete="email"
        autofocus
        :error="fieldError('email')"
      />
      <p v-if="formError" class="pt-2 text-[14px] font-semibold text-red-500" role="alert">{{ formError }}</p>
      <button
        type="submit"
        :disabled="!email || pending"
        class="bg-brand mt-6 w-full rounded-sm py-3 text-[17px] font-semibold text-white disabled:bg-gray-200"
      >
        {{ t("auth.forgot.submit") }}
      </button>
    </template>
  </form>
</template>
