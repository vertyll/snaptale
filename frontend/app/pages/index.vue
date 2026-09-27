<script setup lang="ts">
import type { FeedPage, PostCard } from "~/utils/types";

const requestFetch = useRequestFetch();
const api = useApi();
const { t, errorText } = useMessages();

const posts = ref<PostCard[]>([]);
const page = ref(0);
const hasMore = ref(false);
const loading = ref(false);
const error = ref<string | null>(null);
const sentinel = useTemplateRef<HTMLElement>("sentinel");

const { data: first } = await useAsyncData("feed", () => requestFetch<FeedPage>("/api/posts?page=0"));
watch(
  first,
  (feed) => {
    posts.value = feed?.posts ?? [];
    hasMore.value = feed?.hasMore ?? false;
    page.value = 0;
  },
  { immediate: true }
);

async function loadMore() {
  if (loading.value || !hasMore.value) {
    return;
  }
  loading.value = true;
  error.value = null;
  try {
    const next = await api.get<FeedPage>(`/api/posts?page=${page.value + 1}`);
    const known = new Set(posts.value.map((post) => post.id));
    posts.value.push(...next.posts.filter((post) => !known.has(post.id)));
    hasMore.value = next.hasMore;
    page.value += 1;
  } catch (error_: unknown) {
    error.value = errorText(error_);
  } finally {
    loading.value = false;
  }
}

let observer: IntersectionObserver | null = null;
onMounted(() => {
  observer = new IntersectionObserver(([entry]) => entry?.isIntersecting && loadMore(), { rootMargin: "600px" });
  if (sentinel.value) {
    observer.observe(sentinel.value);
  }
});
onBeforeUnmount(() => observer?.disconnect());
</script>

<template>
  <main class="w-[calc(100%-90px)] max-w-[690px] pt-[80px]">
    <EmailVerificationBanner />
    <PostMain v-for="post in posts" :key="post.id" :post="post" />
    <p v-if="posts.length === 0" class="pt-10 text-center text-gray-500">{{ t("feed.empty") }}</p>
    <p v-if="error" class="py-4 text-center font-semibold text-red-500" role="alert">{{ error }}</p>
    <div ref="sentinel" class="flex justify-center py-6">
      <Icon v-if="loading" name="mdi:loading" size="40" class="text-brand animate-spin" />
    </div>
  </main>
</template>
