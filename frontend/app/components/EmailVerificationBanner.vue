<script setup lang="ts">
const { me } = useSession();
const { errorText } = useMessages();
const api = useApi();
const state = ref<"idle" | "sending" | "sent">("idle");
const error = ref<string | null>(null);

async function resend() {
  state.value = "sending";
  error.value = null;
  try {
    await api.post("/api/auth/email/resend");
    state.value = "sent";
  } catch (failure: unknown) {
    error.value = errorText(failure);
    state.value = "idle";
  }
}
</script>

<template>
  <div v-if="me && !me.emailVerified" class="mb-4 rounded-md border border-amber-300 bg-amber-50 px-4 py-3 text-sm">
    <span>Potwierdź adres e-mail - wysłaliśmy link na {{ me.email }}.</span>
    <span v-if="state === 'sent'" class="pl-1 font-semibold">Wysłano ponownie.</span>
    <button
      v-else
      type="button"
      class="text-brand pl-1 font-semibold disabled:text-gray-400"
      :disabled="state === 'sending'"
      @click="resend"
    >
      Wyślij ponownie
    </button>
    <div v-if="error" class="pt-1 font-semibold text-red-500">{{ error }}</div>
  </div>
</template>
