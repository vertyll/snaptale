<script setup lang="ts">
import type { UserSummary } from "~/utils/types";

const { t } = useMessages();
const route = useRoute();
const { isSignedIn } = useSession();
const requestFetch = useRequestFetch();

const { data: suggested } = await useAsyncData("suggested", () => requestFetch<UserSummary[]>("/api/users/suggested"), {
  default: () => [],
});
const { data: following } = await useAsyncData(
  "following",
  () => (isSignedIn.value ? requestFetch<UserSummary[]>("/api/me/following") : Promise.resolve([])),
  { default: () => [], watch: [isSignedIn] }
);
</script>

<template>
  <nav
    :class="route.path === '/' ? 'lg:w-[310px]' : 'lg:w-[220px]'"
    class="fixed z-20 h-full w-[75px] overflow-auto border-r border-gray-200 bg-white pt-[70px] lg:border-r-0"
    :aria-label="t('nav.side')"
  >
    <div class="w-full pl-2 lg:pl-0">
      <NuxtLink to="/" class="flex w-full items-center rounded-md p-2.5 hover:bg-gray-100 lg:ml-2">
        <Icon name="mdi:home" size="30" class="text-brand mx-auto lg:mx-0" />
        <span class="text-brand hidden pl-[9px] text-[17px] font-semibold lg:block">{{ t("nav.forYou") }}</span>
      </NuxtLink>

      <div class="mt-2 mr-2 border-b border-gray-200 lg:mr-0 lg:ml-2" />

      <div class="hidden px-2 pt-4 pb-2 text-xs font-semibold text-gray-600 lg:block">{{ t("nav.suggested") }}</div>
      <div class="block pt-3 lg:hidden" />
      <UserListItem v-for="user in suggested" :key="user.id" :user="user" class="lg:ml-2" />

      <template v-if="isSignedIn">
        <div class="mt-2 mr-2 border-b border-gray-200 lg:mr-0 lg:ml-2" />
        <div class="hidden px-2 pt-4 pb-2 text-xs font-semibold text-gray-600 lg:block">{{ t("nav.followed") }}</div>
        <div v-if="following.length === 0" class="hidden px-2 text-[13px] text-gray-500 lg:ml-2 lg:block">
          {{ t("nav.noFollowed") }}
        </div>
        <UserListItem v-for="user in following" :key="user.id" :user="user" class="lg:ml-2" />
      </template>

      <div class="mt-2 mr-2 hidden border-b border-gray-200 lg:mr-0 lg:ml-2 lg:block" />
      <div class="hidden px-2 pt-4 text-[11px] text-gray-500 lg:ml-2 lg:block">© 2026 SnapTale</div>
      <div class="pb-14" />
    </div>
  </nav>
</template>
