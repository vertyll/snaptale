<script setup lang="ts">
import type { PostCard } from "~/utils/types";

const props = defineProps<{ post: PostCard }>();
const { toggle } = useLike();
const root = useTemplateRef<HTMLElement>("root");
const video = useTemplateRef<HTMLVideoElement>("video");
let observer: IntersectionObserver | null = null;

onMounted(() => {
  observer = new IntersectionObserver(
    ([entry]) => {
      if (entry?.isIntersecting) {
        void video.value?.play().catch(() => undefined);
      } else {
        video.value?.pause();
      }
    },
    { threshold: [0.6] }
  );
  if (root.value) {
    observer.observe(root.value);
  }
});

onBeforeUnmount(() => {
  observer?.disconnect();
  video.value?.pause();
});
</script>

<template>
  <article ref="root" class="flex border-b border-gray-200 py-6">
    <NuxtLink :to="`/profile/${props.post.author.id}`" class="shrink-0">
      <UserAvatar :url="props.post.author.avatarUrl" :size="60" :alt="props.post.author.name" />
    </NuxtLink>
    <div class="w-full px-4 pl-3">
      <NuxtLink :to="`/profile/${props.post.author.id}`" class="pb-0.5">
        <span class="font-bold hover:underline">{{ handleOf(props.post.author.name) }}</span>
        <span class="pl-1 text-[13px] text-gray-500">{{ props.post.author.name }}</span>
      </NuxtLink>
      <p class="max-w-[300px] pb-0.5 text-[15px] break-words md:max-w-[400px]">{{ props.post.text }}</p>
      <div class="flex items-center pb-0.5 text-[14px] font-semibold">
        <Icon name="mdi:music" size="17" />
        <span class="px-1">oryginalny dźwięk - {{ handleOf(props.post.author.name) }}</span>
      </div>

      <div class="mt-2.5 flex">
        <NuxtLink
          :to="`/post/${props.post.id}`"
          class="relative flex max-h-[580px] min-h-[480px] max-w-[260px] items-center rounded-xl bg-black"
          :aria-label="`Otwórz film: ${props.post.text}`"
        >
          <video
            ref="video"
            :src="props.post.videoUrl"
            loop
            muted
            playsinline
            preload="metadata"
            class="mx-auto h-full rounded-xl object-cover"
          />
          <img
            class="absolute right-2 bottom-14"
            width="90"
            src="~/assets/images/snaptale-logo.png"
            alt=""
            aria-hidden="true"
          />
        </NuxtLink>
        <div class="relative mr-[75px]">
          <div class="absolute bottom-0 pl-2">
            <div class="pb-4 text-center">
              <button
                type="button"
                class="rounded-full bg-gray-200 p-2"
                :aria-pressed="props.post.likedByMe"
                aria-label="Polub"
                @click="toggle(props.post)"
              >
                <Icon name="mdi:heart" size="25" :class="props.post.likedByMe ? 'text-like' : ''" />
              </button>
              <span class="block text-xs font-semibold text-gray-800">{{ props.post.likeCount }}</span>
            </div>
            <NuxtLink :to="`/post/${props.post.id}`" class="block pb-4 text-center" aria-label="Komentarze">
              <span class="inline-block rounded-full bg-gray-200 p-2"
                ><Icon name="mdi:comment-processing" size="25"
              /></span>
              <span class="block text-xs font-semibold text-gray-800">{{ props.post.commentCount }}</span>
            </NuxtLink>
          </div>
        </div>
      </div>
    </div>
  </article>
</template>
