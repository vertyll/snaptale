<script setup lang="ts">
type AvatarSize = 33 | 35 | 40 | 60 | 95 | 120;

/**
 * The classes for each size an avatar is shown at, since a style attribute would need 'unsafe-inline' in the policy: a
 * picture's width and height, and the font size a fallback icon, one em square, is drawn at.
 */
const SIZE_CLASSES: Record<AvatarSize, { picture: string; icon: string }> = {
  33: { picture: "size-[33px]", icon: "text-[33px]" },
  35: { picture: "size-[35px]", icon: "text-[35px]" },
  40: { picture: "size-[40px]", icon: "text-[40px]" },
  60: { picture: "size-[60px]", icon: "text-[60px]" },
  95: { picture: "size-[95px]", icon: "text-[95px]" },
  120: { picture: "size-[120px]", icon: "text-[120px]" },
};

defineProps<{ url: string | null; size: AvatarSize; alt: string }>();
</script>

<template>
  <img
    v-if="url"
    :src="url"
    :alt="alt"
    :width="size"
    :height="size"
    class="shrink-0 rounded-full object-cover"
    :class="SIZE_CLASSES[size].picture"
  />
  <span
    v-else
    class="icon-[mdi--account-circle] shrink-0 text-gray-300"
    :class="SIZE_CLASSES[size].icon"
    :aria-label="alt"
    aria-hidden="true"
  />
</template>
