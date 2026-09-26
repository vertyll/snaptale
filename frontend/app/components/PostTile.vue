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
      <Icon name="mdi:loading" size="60" class="animate-spin text-white" />
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
      <Icon name="mdi:heart" size="14" />{{ post.likeCount }}
      <Icon name="mdi:comment-processing" size="14" class="ml-2" />{{ post.commentCount }}
    </div>
  </NuxtLink>
</template>
