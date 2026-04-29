<template>
  <div id="SideNav" :class="route.fullPath === '/' ? 'lg:w-[310px]' : 'lg:w-[220px]'"
    class="fixed z-20 bg-white pt-[70px] h-full lg:border-r-0 border-r w-[75px] overflow-auto">
    
    <div class="lg:w-full w-full pl-2 lg:pl-0">
      <NuxtLink to="/">
        <MenuItem iconString="Dla Ciebie" colorString="#bc2cf0" sizeString="30" class="lg:ml-2" />
      </NuxtLink>

      <div class="border-b lg:ml-2 mt-2 mr-2 lg:mr-0" />

      <div class="lg:block hidden text-xs text-gray-600 font-semibold pt-4 pb-2 px-2">
        Sugerowane konta
      </div>

      <div class="lg:hidden block pt-3" />

      <div v-if="$generalStore.suggested" v-for="sug in $generalStore.suggested" :key="sug.id">
        <div @click="isLoggedIn(sug)" class="cursor-pointer">
          <MenuItemFollow :user="sug" class="lg:ml-2" />
        </div>
      </div>

      <button class="lg:block hidden text-[#bc2cf0] pt-1.5 pl-2 text-[13px] lg:ml-2">
        Zobacz wszystkie
      </button>

      <div class="lg:block hidden border-b lg:ml-2 mt-2 mr-2 lg:mr-0" />

      <div class="lg:block hidden text-[11px] text-gray-500">
        <div class="pt-4 px-2 lg:ml-2">© 2026 SnapTale</div>
      </div>

      <div class="pb-14"></div>
    </div>
  </div>
</template>

<script setup>
const { $generalStore, $userStore } = useNuxtApp();
const route = useRoute();
const router = useRouter();

const isLoggedIn = (fol) => {
  if (!$userStore.id) {
    $generalStore.isLoginOpen = true;
    return;
  }
  setTimeout(() => router.push(`/profile/${fol.id}`), 200);
};
</script>
