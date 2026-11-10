<script setup lang="ts">
const { open } = useOverlays();
const { locale } = useLocale();
const { t } = useMessages();

useHead({
  htmlAttrs: { lang: locale },
  meta: [{ name: "description", content: () => t("app.description") }],
});

watch(open, (overlay) => document.body.classList.toggle("overflow-hidden", overlay !== null));
</script>

<template>
  <NuxtLayout>
    <NuxtPage />
  </NuxtLayout>
  <EditProfileOverlay v-if="open === 'editProfile'" />
</template>
