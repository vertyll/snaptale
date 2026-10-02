<script setup lang="ts">
import { LOCALES, type Locale } from "~/composables/useLocale";

const { t } = useMessages();
const { locale, setLocale } = useLocale();
const open = ref(false);
const root = useTemplateRef<HTMLElement>("root");

useClickOutside(root, () => (open.value = false));

async function choose(next: Locale) {
  open.value = false;
  if (next !== locale.value) {
    await setLocale(next);
  }
}
</script>

<template>
  <div ref="root" class="relative">
    <button
      type="button"
      class="flex items-center gap-1 rounded-sm border border-gray-300 px-2 py-[6px] text-[15px] font-medium hover:bg-gray-100"
      :aria-label="t('nav.language')"
      :aria-expanded="open"
      aria-haspopup="menu"
      @click="open = !open"
    >
      <span>{{ locale.toUpperCase() }}</span>
      <Icon :name="open ? 'mdi:chevron-up' : 'mdi:chevron-down'" size="18" />
    </button>
    <div
      v-if="open"
      role="menu"
      class="absolute top-[43px] right-0 w-[80px] rounded-lg border border-gray-200 bg-white py-1.5 shadow-xl"
    >
      <button
        v-for="option in LOCALES"
        :key="option"
        type="button"
        role="menuitemradio"
        :aria-checked="option === locale"
        :class="option === locale ? 'text-brand' : ''"
        class="block w-full px-3 py-2 text-left text-sm font-semibold hover:bg-gray-100"
        @click="choose(option)"
      >
        {{ option.toUpperCase() }}
      </button>
    </div>
  </div>
</template>
