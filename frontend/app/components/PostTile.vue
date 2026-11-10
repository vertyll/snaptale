<script setup lang="ts">
import type { PostCard } from "~/utils/types";

defineProps<{ post: PostCard }>();
const video = useTemplateRef<HTMLVideoElement>("video");
const loaded = ref(false);
</script>

<template>
  <NuxtLink
    :to="`/post/${post.id}`"
    class="relative block brightness-90 hover:brightness-110"
    @mouseenter="video?.play().catch(() => undefined)"
    @mouseleave="video?.pause()"
  >
    <div
      v-if="!loaded"
      class="absolute top-0 left-0 flex aspect-[3/4] w-full items-center justify-center rounded-md bg-black"
    >
      <span class="icon-[mdi--loading] text-[60px] animate-spin text-white" aria-hidden="true" />
    </div>
    <video
      ref="video"
      :src="post.videoUrl"
      muted
      loop
      playsinline
      preload="metadata"
      class="aspect-[3/4] w-full rounded-md object-cover"
      @loadeddata="loaded = true"
    />
    <div class="px-1 pt-1 text-[15px] break-words text-gray-700">{{ post.text }}</div>
    <div class="flex items-center gap-1 px-1 text-xs font-bold text-gray-600">
      <span class="icon-[mdi--heart] text-[14px]" aria-hidden="true" />{{ post.likeCount }}
      <span class="icon-[mdi--comment-processing] text-[14px] ml-2" aria-hidden="true" />{{ post.commentCount }}
    </div>
  </NuxtLink>
</template>
