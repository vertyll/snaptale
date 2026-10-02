<script setup lang="ts">
import { LOCALES, type Locale } from "~/composables/useLocale";

const { t } = useMessages();
const { locale, setLocale } = useLocale();
const { me, signIn, logout } = useSession();
const { whenSignedIn } = useOverlays();
const accountUrl = useRuntimeConfig().public.keycloakAccountUrl;
const route = useRoute();
const menuOpen = ref(false);
const menu = useTemplateRef<HTMLElement>("menu");

useClickOutside(menu, () => (menuOpen.value = false));

function upload() {
  whenSignedIn(() => navigateTo("/upload"));
}

function changeLanguage(event: Event) {
  void setLocale((event.target as HTMLSelectElement).value as Locale);
}

async function signOut() {
  menuOpen.value = false;
  await logout();
}
</script>

<template>
  <header class="fixed z-30 flex h-[61px] w-full items-center border-b border-gray-200 bg-white">
    <div
      :class="route.path === '/' ? 'max-w-[1150px]' : ''"
      class="mx-auto flex w-full items-center justify-between px-6"
    >
      <NuxtLink to="/" :aria-label="t('nav.home')">
        <img width="115" src="~/assets/images/snaptale-logo.png" alt="SnapTale" />
      </NuxtLink>

      <div class="flex w-full max-w-[400px] min-w-[275px] items-center justify-end gap-3">
        <select
          :value="locale"
          :aria-label="t('nav.language')"
          class="rounded-sm border border-gray-300 bg-white px-2 py-[7px] text-[15px] font-medium hover:bg-gray-100"
          @change="changeLanguage"
        >
          <option v-for="option in LOCALES" :key="option" :value="option">{{ option.toUpperCase() }}</option>
        </select>
        <button
          type="button"
          class="flex items-center rounded-sm border border-gray-300 px-3 py-[6px] hover:bg-gray-100"
          @click="upload"
        >
          <Icon name="mdi:plus" size="22" />
          <span class="px-2 text-[15px] font-medium">{{ t("nav.upload") }}</span>
        </button>

        <button
          v-if="!me"
          type="button"
          class="bg-brand rounded-md px-7 py-[6px] text-[15px] font-medium text-white"
          @click="signIn"
        >
          {{ t("nav.signIn") }}
        </button>
        <div v-else ref="menu" class="relative">
          <button
            type="button"
            class="mt-1"
            :aria-expanded="menuOpen"
            :aria-label="t('nav.accountMenu')"
            @click="menuOpen = !menuOpen"
          >
            <UserAvatar :url="me.avatarUrl" :size="33" :alt="me.name" />
          </button>
          <div
            v-if="menuOpen"
            class="absolute top-[43px] -right-2 w-[200px] rounded-lg border border-gray-200 bg-white py-1.5 shadow-xl"
          >
            <NuxtLink
              :to="`/profile/${me.id}`"
              class="flex items-center px-2 py-3 hover:bg-gray-100"
              @click="menuOpen = false"
            >
              <Icon name="mdi:account-outline" size="20" />
              <span class="pl-2 text-sm font-semibold">{{ t("nav.profile") }}</span>
            </NuxtLink>
            <a
              v-if="accountUrl"
              :href="`${accountUrl}?kc_locale=${locale}`"
              class="flex items-center px-2 py-3 hover:bg-gray-100"
              @click="menuOpen = false"
            >
              <Icon name="mdi:cog-outline" size="20" />
              <span class="pl-2 text-sm font-semibold">{{ t("nav.account") }}</span>
            </a>
            <button
              type="button"
              class="flex w-full items-center border-t border-gray-200 px-2 py-3 hover:bg-gray-100"
              @click="signOut"
            >
              <Icon name="mdi:logout" size="20" />
              <span class="pl-2 text-sm font-semibold">{{ t("nav.logout") }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>
