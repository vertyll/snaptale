<script setup lang="ts">
import type { ProfileResponse } from "~/utils/types";

const route = useRoute();
const requestFetch = useRequestFetch();
const api = useApi();
const { me } = useSession();
const { open, whenSignedIn } = useOverlays();
const { t, errorText } = useMessages();
const userId = computed(() => Number(route.params.id));
const followError = ref<string | null>(null);

const { data: profile, error } = await useAsyncData(
  () => `profile-${userId.value}`,
  () => requestFetch<ProfileResponse>(`/api/users/${userId.value}`)
);
if (error.value) {
  throw createError({
    status: 404,
    message: t("profile.notFound"),
    data: { message: t("profile.notFound") },
    fatal: true,
  });
}

const isMe = computed(() => me.value?.id === userId.value);
const totalLikes = computed(() => profile.value?.posts.reduce((sum, post) => sum + post.likeCount, 0) ?? 0);

useHead(() => ({ title: profile.value ? `${profile.value.user.name} | SnapTale` : "SnapTale" }));

function toggleFollow() {
  whenSignedIn(async () => {
    const current = profile.value;
    if (!current) {
      return;
    }
    const following = current.followedByMe;
    followError.value = null;
    try {
      await (following ? api.del(`/api/users/${userId.value}/follow`) : api.put(`/api/users/${userId.value}/follow`));
      current.followedByMe = !following;
      current.follows.followers += following ? -1 : 1;
      await refreshNuxtData("following");
    } catch (failure: unknown) {
      followError.value = errorText(failure);
    }
  });
}
</script>

<template>
  <main
    v-if="profile"
    class="w-[calc(100%-90px)] max-w-[1800px] pt-[90px] pr-2 lg:pr-0 lg:pl-[160px] 2xl:mx-auto 2xl:pl-[185px]"
  >
    <div class="flex">
      <UserAvatar :url="profile.user.avatarUrl" :size="120" :alt="profile.user.name" />
      <div class="ml-5 min-w-0">
        <h1 class="truncate text-[30px] font-bold">{{ handleOf(profile.user.name) }}</h1>
        <div class="truncate text-[18px]">{{ profile.user.name }}</div>
      </div>
    </div>

    <div class="flex pt-2">
      <button
        v-if="isMe"
        type="button"
        class="mt-3 flex items-center rounded-md border border-gray-300 px-3.5 py-1.5 text-[15px] font-semibold hover:bg-gray-100"
        @click="open = 'editProfile'"
      >
        <Icon name="mdi:pencil" size="18" class="mr-1" />{{ t("profile.edit") }}
      </button>
      <button
        v-else
        type="button"
        :class="profile.followedByMe ? 'bg-gray-300' : 'bg-brand text-white'"
        class="mt-3 rounded-md px-8 py-1.5 text-[15px] font-semibold"
        @click="toggleFollow"
      >
        {{ profile.followedByMe ? t("profile.unfollow") : t("profile.follow") }}
      </button>
    </div>
    <p v-if="followError" class="pt-2 font-semibold text-red-500" role="alert">{{ followError }}</p>

    <div class="flex items-center gap-4 pt-4">
      <div>
        <span class="font-bold">{{ profile.follows.following }}</span>
        <span class="pl-1.5 text-[15px] font-light text-gray-500">{{
          t("profile.following", { count: profile.follows.following })
        }}</span>
      </div>
      <div>
        <span class="font-bold">{{ profile.follows.followers }}</span>
        <span class="pl-1.5 text-[15px] font-light text-gray-500">{{
          t("profile.followers", { count: profile.follows.followers })
        }}</span>
      </div>
      <div>
        <span class="font-bold">{{ totalLikes }}</span>
        <span class="pl-1.5 text-[15px] font-light text-gray-500">{{ t("profile.likes", { count: totalLikes }) }}</span>
      </div>
    </div>
    <p v-if="profile.user.bio" class="max-w-[500px] pt-4 text-[15px] font-light text-gray-500">
      {{ profile.user.bio }}
    </p>

    <div class="flex w-full items-center border-b border-gray-200 pt-4">
      <div class="w-60 border-b-2 border-black py-2 text-center text-[17px] font-semibold">
        {{ t("profile.videos") }}
      </div>
    </div>
    <p v-if="profile.posts.length === 0" class="pt-8 text-gray-500">{{ t("profile.noVideos") }}</p>
    <div class="mt-4 grid grid-cols-2 gap-3 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 2xl:grid-cols-6">
      <PostTile v-for="post in profile.posts" :key="post.id" :post="post" />
    </div>
  </main>
</template>
