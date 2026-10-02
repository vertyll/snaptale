<script setup lang="ts">
const props = defineProps<{ document: "terms" | "privacy" }>();
const { t } = useMessages();

useHead({ title: () => `${t(`legal.${props.document}.title`)} | SnapTale` });
</script>

<template>
  <main class="w-[calc(100%-90px)] max-w-[690px] pt-[90px] pb-16">
    <h1 class="pb-6 text-[28px] font-bold">{{ t(`legal.${props.document}.title`) }}</h1>
    <p
      v-for="paragraph in t(`legal.${props.document}.content`).split('\n\n')"
      :key="paragraph"
      class="pb-4 text-[15px] leading-relaxed text-gray-800"
    >
      {{ paragraph }}
    </p>
  </main>
</template>
