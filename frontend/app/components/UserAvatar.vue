<script setup lang="ts">
defineProps<{ url: string | null; size: number; alt: string }>();
</script>

<template>
  <img
    v-if="url"
    :src="url"
    :alt="alt"
    :width="size"
    :height="size"
    class="shrink-0 rounded-full object-cover"
    :style="{ width: `${size}px`, height: `${size}px` }"
  />
  <Icon v-else name="mdi:account-circle" :size="`${size}px`" class="shrink-0 text-gray-300" :aria-label="alt" />
</template>
