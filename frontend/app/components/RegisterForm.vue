<script setup lang="ts">
const { refresh } = useSession();
const { close } = useOverlays();
const api = useApi();
const { submit, pending, fieldError, formError } = useForm();
const name = ref("");
const email = ref("");
const password = ref("");
const confirmPassword = ref("");
const mismatch = computed(() => confirmPassword.value !== "" && confirmPassword.value !== password.value);

async function register() {
  if (mismatch.value) {
    return;
  }
  const ok = await submit(async () => {
    await api.post("/api/auth/register", { name: name.value, email: email.value, password: password.value });
    await refresh();
  });
  if (ok) {
    close();
    await refreshNuxtData();
  }
}
</script>

<template>
  <form class="px-6" @submit.prevent="register">
    <h2 class="mb-4 text-center text-[28px] font-bold">Zarejestruj się</h2>
    <TextField
      v-model="name"
      label="Pełna nazwa"
      autocomplete="name"
      :maxlength="50"
      autofocus
      :error="fieldError('name')"
      class="pb-2"
    />
    <TextField
      v-model="email"
      label="Adres e-mail"
      type="email"
      autocomplete="email"
      :error="fieldError('email')"
      class="pb-2"
    />
    <TextField
      v-model="password"
      label="Hasło (min. 8 znaków)"
      type="password"
      autocomplete="new-password"
      :maxlength="72"
      :error="fieldError('password')"
      class="pb-2"
    />
    <TextField
      v-model="confirmPassword"
      label="Potwierdź hasło"
      type="password"
      autocomplete="new-password"
      :maxlength="72"
      :error="mismatch ? 'Hasła nie są takie same.' : undefined"
      class="pb-2"
    />
    <p v-if="formError" class="pt-2 text-[14px] font-semibold text-red-500" role="alert">{{ formError }}</p>
    <button
      type="submit"
      :disabled="!name || !email || !password || !confirmPassword || mismatch || pending"
      class="bg-brand mt-6 w-full rounded-sm py-3 text-[17px] font-semibold text-white disabled:bg-gray-200"
    >
      Zarejestruj się
    </button>
  </form>
</template>
