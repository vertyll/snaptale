<script setup lang="ts">
const props = defineProps<{ document: "terms" | "privacy" }>();
const { t } = useMessages();
const title = computed(() => t(`legal.${props.document}.title`));
// Each section is a heading and a paragraph, `legal.<document>.h<n>` and `p<n>` in the catalogue.
const SECTIONS = { terms: 5, privacy: 8 } as const;
const sections = computed(() =>
  Array.from({ length: SECTIONS[props.document] }, (_, index) => ({
    heading: t(`legal.${props.document}.h${index + 1}`),
    text: t(`legal.${props.document}.p${index + 1}`),
  }))
);

useHead({ title: () => `${title.value} | SnapTale` });
</script>

<template>
  <main class="w-[calc(100%-75px)] px-4 pt-[90px] pb-16 lg:w-[calc(100%-220px)]">
    <article class="mx-auto max-w-[690px]">
      <h1 class="text-[28px] font-bold">{{ title }}</h1>
      <p class="pt-1 pb-6 text-[13px] text-gray-500">
        {{ t("legal.lastModified") }}: {{ t(`legal.${props.document}.lastModified`) }}
      </p>
      <section v-for="section in sections" :key="section.text" class="pb-4">
        <h2 class="pb-1 text-[17px] font-semibold">{{ section.heading }}</h2>
        <p class="text-[15px] leading-relaxed text-gray-800">
          <template v-for="segment in splitEmails(section.text)" :key="segment.at">
            <a v-if="segment.kind === 'email'" :href="`mailto:${segment.email}`" class="text-brand hover:underline">{{
              segment.email
            }}</a>
            <template v-else>{{ segment.text }}</template>
          </template>
        </p>
      </section>
    </article>
  </main>
</template>
