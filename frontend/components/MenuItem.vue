<template>
  <div class="w-full flex items-center hover:bg-gray-100 p-2.5 rounded-md">
    <div class="flex items-center lg:mx-0 mx-auto">
      <Icon :name="icon" :color="colorString" :size="sizeString" />
      <span
        :style="{ color: colorString }"
        class="lg:block hidden pl-[9px] mt-0.5 font-semibold text-[17px]"
      >
        {{ iconString }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed, toRefs } from 'vue';

const props = defineProps(["iconString", "colorString", "sizeString"]);
const { iconString, colorString, sizeString } = toRefs(props);

const icon = computed(() => {
  if (iconString.value === "Dla Ciebie" || iconString.value === "For You") return "mdi:home";
  if (iconString.value === "Obserwowani" || iconString.value === "Following") return "ci:group";
  if (iconString.value === "LIVE") return "ri:live-line";
  
  return "mdi:help-circle-outline";
});
</script>
