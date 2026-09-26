<script setup lang="ts">
const { t } = useMessages();
const { me, logout } = useSession();
const { open, whenSignedIn } = useOverlays();
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
  await navigateTo("/");
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

      <div class="flex w-full max-w-[320px] min-w-[275px] items-center justify-end gap-3">
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
          @click="open = 'login'"
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
