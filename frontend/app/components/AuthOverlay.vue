<script setup lang="ts">
const { t } = useMessages();
const { open, close } = useOverlays();
</script>

<template>
  <div
    class="fixed top-0 left-0 z-50 flex h-full w-full items-center justify-center bg-black/50"
    role="dialog"
    aria-modal="true"
    @keydown.esc="close"
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
  </div>
</template>
