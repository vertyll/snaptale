<script setup lang="ts">
const { t } = useMessages();
const { locale } = useLocale();
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

async function signOut() {
  menuOpen.value = false;
  await logout();
}
</script>

<template>
  <header class="fixed z-30 flex h-[61px] w-full items-center border-b border-gray-200 bg-white">
    <div
      :class="route.path === '/' ? 'max-w-[1150px]' : ''"
      class="mx-auto flex w-full items-center justify-between gap-2 px-3 sm:px-6"
    >
      <NuxtLink to="/" :aria-label="t('nav.home')">
        <img src="~/assets/images/snaptale-logo.png" alt="SnapTale" class="w-[90px] sm:w-[115px]" />
      </NuxtLink>

      <div class="flex items-center justify-end gap-2 sm:gap-3">
        <LanguageMenu />
        <button
          type="button"
          class="flex items-center rounded-sm border border-gray-300 px-2 py-[6px] hover:bg-gray-100 sm:px-3"
          :aria-label="t('nav.upload')"
          @click="upload"
        >
          <Icon name="mdi:plus" size="22" />
          <span class="hidden px-2 text-[15px] font-medium sm:inline">{{ t("nav.upload") }}</span>
        </button>

        <button
          v-if="!me"
          type="button"
          class="bg-brand rounded-md px-4 py-[6px] text-[15px] font-medium whitespace-nowrap text-white sm:px-7"
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
