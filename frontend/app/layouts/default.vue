<script setup lang="ts">
const route = useRoute();
const wide = computed(() => route.path === "/");
</script>

<template>
  <div>
    <TopNav />
    <div :class="wide ? 'max-w-[1140px]' : ''" class="mx-auto flex w-full justify-between px-0 lg:px-2.5">
      <div class="shrink-0">
        <SideNav />
      </div>
      <slot />
    </div>
    <SiteFooter />
    <CookieBanner />
  </div>
</template>
