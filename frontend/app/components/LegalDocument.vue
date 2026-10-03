<script setup lang="ts">
const props = defineProps<{ document: "terms" | "privacy" }>();
const { t } = useMessages();
const title = computed(() => t(`legal.${props.document}.title`));
const paragraphs = computed(() => t(`legal.${props.document}.content`).split("\n\n"));

useHead({ title: () => `${title.value} | SnapTale` });
</script>

<template>
  <main class="w-[calc(100%-90px)] max-w-[690px] pt-[90px] pb-16">
    <h1 class="pb-6 text-[28px] font-bold">{{ title }}</h1>
    <p v-for="paragraph in paragraphs" :key="paragraph" class="pb-4 text-[15px] leading-relaxed text-gray-800">
      <template v-for="segment in splitEmails(paragraph)" :key="segment.at">
        <a v-if="segment.kind === 'email'" :href="`mailto:${segment.email}`" class="text-brand hover:underline">{{
          segment.email
        }}</a>
        <template v-else>{{ segment.text }}</template>
      </template>
    </p>
  </main>
</template>
