<script setup lang="ts">
const { t } = useMessages();
const { open, close } = useOverlays();
const dialog = ref<HTMLDialogElement | null>(null);

onMounted(() => dialog.value?.showModal());
</script>

<template>
  <dialog
    ref="dialog"
    class="fixed inset-0 z-50 m-0 h-full max-h-none w-full max-w-none items-center justify-center border-0 bg-black/50 p-0 backdrop:bg-transparent open:flex"
    @cancel.prevent="close"
  >
    <div class="relative flex min-h-[70%] w-full max-w-[470px] flex-col rounded-lg bg-white p-4">
      <div class="flex w-full justify-end">
        <button type="button" class="rounded-full bg-gray-100 p-1.5" :aria-label="t('common.close')" @click="close">
          <Icon name="mdi:close" size="26" />
        </button>
      </div>

      <div class="flex-1 pb-20">
        <LoginForm v-if="open === 'login'" />
        <RegisterForm v-else-if="open === 'register'" />
        <ForgotPasswordForm v-else />
      </div>

      <div
        class="absolute bottom-0 left-0 flex w-full items-center justify-center border-t border-gray-200 py-5 text-[14px]"
      >
        <template v-if="open === 'login'">
          <span class="text-gray-600">{{ t("auth.noAccount") }}</span>
          <button type="button" class="text-brand-dark pl-1 font-semibold" @click="open = 'register'">
            {{ t("auth.register") }}
          </button>
        </template>
        <template v-else>
          <span class="text-gray-600">{{ t("auth.haveAccount") }}</span>
          <button type="button" class="text-brand-dark pl-1 font-semibold" @click="open = 'login'">
            {{ t("auth.login") }}
          </button>
        </template>
      </div>
    </div>
  </dialog>
</template>
