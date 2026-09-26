<script setup lang="ts">
const model = defineModel<string>({ required: true });
withDefaults(
  defineProps<{
    label: string;
    type?: "text" | "email" | "password";
    autocomplete?: string;
    maxlength?: number;
    error?: string;
    autofocus?: boolean;
  }>(),
  { type: "text", autocomplete: "off", maxlength: undefined, error: undefined, autofocus: false }
);
const id = useId();
</script>

<template>
  <div>
    <label :for="id" class="sr-only">{{ label }}</label>
    <input
      :id="id"
      v-model="model"
      :type="type"
      :placeholder="label"
      :autocomplete="autocomplete"
      :maxlength="maxlength"
      :autofocus="autofocus"
      :aria-invalid="Boolean(error)"
      class="bg-surface block w-full rounded-md border border-gray-300 px-3 py-2.5 text-gray-800 focus:outline-none"
    />
    <span v-if="error" class="text-[14px] font-semibold text-red-500">{{ error }}</span>
  </div>
</template>
