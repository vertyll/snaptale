<script setup lang="ts">
const { t } = useMessages();
</script>

<template>
  <footer class="border-t border-gray-200 py-4 pl-[85px] text-[12px] text-gray-500 lg:hidden">
    <nav class="flex flex-wrap gap-x-4 gap-y-1" :aria-label="t('nav.legal')">
      <NuxtLink to="/terms" class="hover:underline">{{ t("legal.terms.title") }}</NuxtLink>
      <NuxtLink to="/privacy" class="hover:underline">{{ t("legal.privacy.title") }}</NuxtLink>
    </nav>
    <p class="pt-1">© 2026 SnapTale</p>
  </footer>
</template>
