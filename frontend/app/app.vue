<script setup lang="ts">
const { open } = useOverlays();
const { locale } = useLocale();

useHead({ htmlAttrs: { lang: locale } });

watch(open, (overlay) => document.body.classList.toggle("overflow-hidden", overlay !== null));
</script>

<template>
  <NuxtLayout>
    <NuxtPage />
  </NuxtLayout>
  <EditProfileOverlay v-if="open === 'editProfile'" />
</template>
