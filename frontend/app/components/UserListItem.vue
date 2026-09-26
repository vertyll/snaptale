<script setup lang="ts">
import type { UserSummary } from "~/utils/types";

defineProps<{ user: UserSummary }>();
</script>

<template>
  <NuxtLink :to="`/profile/${user.id}`" class="flex w-full items-center rounded-md px-2 py-1.5 hover:bg-gray-100">
    <UserAvatar :url="user.avatarUrl" :size="35" :alt="user.name" class="mx-auto lg:mx-0" />
    <div class="hidden lg:block lg:pl-2.5">
      <div class="text-[14px] font-bold">{{ handleOf(user.name) }}</div>
      <div class="text-[12px] font-light text-gray-600">{{ user.name }}</div>
    </div>
  </NuxtLink>
</template>
