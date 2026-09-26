<script setup lang="ts">
const { refresh } = useSession();
const { open, close } = useOverlays();
const api = useApi();
const { submit, pending, fieldError, formError } = useForm();
const email = ref("");
const password = ref("");

async function login() {
  const ok = await submit(async () => {
    await api.post("/api/auth/login", { email: email.value, password: password.value });
    await refresh();
  });
  if (ok) {
    close();
    await refreshNuxtData();
  }
}
</script>

<template>
  <form class="px-6" @submit.prevent="login">
    <h2 class="mb-4 text-center text-[28px] font-bold">Zaloguj się</h2>
    <TextField
      v-model="email"
      label="Adres e-mail"
      type="email"
      autocomplete="email"
      autofocus
      :error="fieldError('email')"
      class="pb-2"
    />
    <TextField
      v-model="password"
      label="Hasło"
      type="password"
      autocomplete="current-password"
      :error="fieldError('password')"
      class="pb-2"
    />
    <button type="button" class="text-brand text-[13px] font-semibold" @click="open = 'forgotPassword'">
      Nie pamiętasz hasła?
    </button>
    <p v-if="formError" class="pt-2 text-[14px] font-semibold text-red-500" role="alert">{{ formError }}</p>
    <button
      type="submit"
      :disabled="!email || !password || pending"
      class="bg-brand mt-6 w-full rounded-sm py-3 text-[17px] font-semibold text-white disabled:bg-gray-200"
    >
      Zaloguj
    </button>
  </form>
</template>
