<script setup lang="ts">
import type { CommentView, PostDetails } from "~/utils/types";

definePageMeta({ layout: false });

const route = useRoute();
const router = useRouter();
const requestFetch = useRequestFetch();
const api = useApi();
const { me } = useSession();
const { whenSignedIn } = useOverlays();
const { toggle } = useLike();
const { t, errorText } = useMessages();

const postId = computed(() => Number(route.params.id));
const comment = ref("");
const actionError = ref<string | null>(null);
const sending = ref(false);
const loaded = ref(false);
const commentList = useTemplateRef<HTMLElement>("commentList");

const { data: details, error } = await useAsyncData(
  () => `post-${postId.value}`,
  () => requestFetch<PostDetails>(`/api/posts/${postId.value}`)
);
if (error.value) {
  throw createError({ status: 404, message: t("post.notFound"), data: { message: t("post.notFound") }, fatal: true });
}
watch(postId, () => (loaded.value = false));

useHead(() => ({ title: details.value ? `${details.value.post.text} | SnapTale` : "SnapTale" }));

const neighbours = computed(() => {
  const ids = details.value?.authorPostIds ?? [];
  const index = ids.indexOf(postId.value);
  return {
    newer: index > 0 ? ids[index - 1] : undefined,
    older: index >= 0 && index < ids.length - 1 ? ids[index + 1] : undefined,
  };
});

function goTo(id: number | undefined) {
  if (id !== undefined) {
    void router.replace(`/post/${id}`);
  }
}

function back() {
  if (window.history.length > 1) {
    router.back();
  } else {
    void navigateTo("/");
  }
}

function canDelete(item: CommentView): boolean {
  return me.value !== null && (me.value.id === item.author.id || me.value.id === details.value?.post.author.id);
}

async function addComment() {
  const current = details.value;
  if (!current || !comment.value.trim()) {
    return;
  }
  sending.value = true;
  actionError.value = null;
  try {
    const created = await api.post<CommentView>(`/api/posts/${current.post.id}/comments`, { text: comment.value });
    current.comments.push(created);
    current.post.commentCount += 1;
    comment.value = "";
    await nextTick();
    commentList.value?.scrollTo({ top: commentList.value.scrollHeight, behavior: "smooth" });
  } catch (failure: unknown) {
    actionError.value = errorText(failure);
  } finally {
    sending.value = false;
  }
}

async function deleteComment(item: CommentView) {
  const current = details.value;
  if (!current || !confirm(t("post.confirmDeleteComment"))) {
    return;
  }
  try {
    await api.del(`/api/posts/${current.post.id}/comments/${item.id}`);
    current.comments = current.comments.filter((candidate) => candidate.id !== item.id);
    current.post.commentCount -= 1;
  } catch (failure: unknown) {
    actionError.value = errorText(failure);
  }
}

async function deletePost() {
  const current = details.value;
  if (!current || !confirm(t("post.confirmDelete"))) {
    return;
  }
  try {
    await api.del(`/api/posts/${current.post.id}`);
    await navigateTo(`/profile/${current.post.author.id}`);
  } catch (failure: unknown) {
    actionError.value = errorText(failure);
  }
}

function onKey(event: KeyboardEvent) {
  if (event.key === "ArrowUp") {
    goTo(neighbours.value.newer);
  } else if (event.key === "ArrowDown") {
    goTo(neighbours.value.older);
  } else if (event.key === "Escape") {
    back();
  }
}
onMounted(() => window.addEventListener("keydown", onKey));
onBeforeUnmount(() => window.removeEventListener("keydown", onKey));
</script>

<template>
  <div
    v-if="details"
    class="fixed top-0 left-0 z-50 h-full w-full justify-between overflow-auto bg-black lg:flex lg:overflow-hidden"
  >
    <div class="relative h-full lg:w-[calc(100%-540px)]">
      <button
        type="button"
        class="absolute z-20 m-5 rounded-full bg-gray-700 p-1.5 hover:bg-gray-800"
        :aria-label="t('common.close')"
        @click="back"
      >
        <Icon name="mdi:close" size="27" class="text-white" />
      </button>
      <div class="absolute top-4 right-4 z-20 flex flex-col gap-3">
        <button
          type="button"
          class="rounded-full bg-gray-700 p-1.5 hover:bg-gray-800 disabled:opacity-40"
          :disabled="neighbours.newer === undefined"
          :aria-label="t('post.newer')"
          @click="goTo(neighbours.newer)"
        >
          <Icon name="mdi:chevron-up" size="30" class="text-white" />
        </button>
        <button
          type="button"
          class="rounded-full bg-gray-700 p-1.5 hover:bg-gray-800 disabled:opacity-40"
          :disabled="neighbours.older === undefined"
          :aria-label="t('post.older')"
          @click="goTo(neighbours.older)"
        >
          <Icon name="mdi:chevron-down" size="30" class="text-white" />
        </button>
      </div>
      <img
        class="absolute top-[18px] left-[70px] z-20 rounded-full"
        width="45"
        src="~/assets/images/snaptale-logo-mini.png"
        alt=""
        aria-hidden="true"
      />
      <div v-if="!loaded" class="absolute inset-0 z-10 flex items-center justify-center bg-black/70">
        <Icon name="mdi:loading" size="100" class="animate-spin text-white" />
      </div>
      <video
        :key="details.post.id"
        :src="details.post.videoUrl"
        class="mx-auto h-screen"
        autoplay
        loop
        muted
        playsinline
        controls
        @loadeddata="loaded = true"
      />
    </div>

    <aside class="relative flex h-full w-full flex-col bg-white lg:max-w-[550px]">
      <div class="flex items-center justify-between px-8 pt-7">
        <NuxtLink :to="`/profile/${details.post.author.id}`" class="flex items-center">
          <UserAvatar :url="details.post.author.avatarUrl" :size="40" :alt="details.post.author.name" />
          <div class="ml-3">
            <div class="text-[17px] font-semibold">{{ handleOf(details.post.author.name) }}</div>
            <div class="text-[13px] font-light">
              {{ details.post.author.name }} · <span class="font-medium">{{ formatDate(details.post.createdAt) }}</span>
            </div>
          </div>
        </NuxtLink>
        <button
          v-if="me?.id === details.post.author.id"
          type="button"
          :aria-label="t('post.delete')"
          @click="deletePost"
        >
          <Icon name="mdi:delete-outline" size="25" />
        </button>
      </div>

      <p class="mt-4 px-8 text-sm break-words">{{ details.post.text }}</p>
      <div class="mt-4 flex items-center px-8 text-sm font-bold">
        <Icon name="mdi:music" size="17" />
        <span class="pl-1">{{ t("common.originalSound", { handle: handleOf(details.post.author.name) }) }}</span>
      </div>

      <div class="mt-6 flex items-center gap-4 px-8 pb-4">
        <button
          type="button"
          class="flex items-center"
          :aria-pressed="details.post.likedByMe"
          @click="toggle(details.post)"
        >
          <span class="rounded-full bg-gray-200 p-2">
            <Icon name="mdi:heart" size="25" :class="details.post.likedByMe ? 'text-like' : ''" />
          </span>
          <span class="pl-2 text-xs font-semibold text-gray-800">{{ details.post.likeCount }}</span>
        </button>
        <div class="flex items-center">
          <span class="rounded-full bg-gray-200 p-2"><Icon name="mdi:comment-processing" size="25" /></span>
          <span class="pl-2 text-xs font-semibold text-gray-800">{{ details.post.commentCount }}</span>
        </div>
      </div>
      <p v-if="actionError" class="px-8 pb-2 font-semibold text-red-500" role="alert">{{ actionError }}</p>

      <div ref="commentList" class="flex-1 overflow-auto border-t-2 border-gray-200 bg-[#F8F8F8] pb-28">
        <p v-if="details.comments.length === 0" class="mt-6 text-center text-xl text-gray-500">
          {{ t("post.noComments") }}
        </p>
        <div v-for="item in details.comments" :key="item.id" class="mt-4 flex items-start px-8">
          <NuxtLink :to="`/profile/${item.author.id}`">
            <UserAvatar :url="item.author.avatarUrl" :size="40" :alt="item.author.name" />
          </NuxtLink>
          <div class="ml-4 w-full">
            <div class="flex items-center justify-between text-[18px] font-semibold">
              {{ item.author.name }}
              <button
                v-if="canDelete(item)"
                type="button"
                :aria-label="t('post.deleteComment')"
                @click="deleteComment(item)"
              >
                <Icon name="mdi:delete-outline" size="22" />
              </button>
            </div>
            <p class="text-[15px] font-light break-words">{{ item.text }}</p>
          </div>
        </div>
      </div>

      <form
        v-if="me"
        class="absolute bottom-0 flex h-[85px] w-full items-center justify-between border-t-2 border-gray-200 bg-white px-8 py-5"
        @submit.prevent="addComment"
      >
        <label for="new-comment" class="sr-only">{{ t("post.addComment") }}</label>
        <input
          id="new-comment"
          v-model="comment"
          maxlength="500"
          class="bg-surface w-full rounded-lg border-2 border-transparent p-2 text-[14px] focus:border-gray-400 focus:outline-none"
          type="text"
          :placeholder="t('post.commentPlaceholder')"
        />
        <button
          type="submit"
          :disabled="!comment.trim() || sending"
          class="text-brand ml-5 pr-1 text-sm font-semibold disabled:text-gray-400"
        >
          {{ t("common.publish") }}
        </button>
      </form>
      <div v-else class="absolute bottom-0 w-full border-t-2 border-gray-200 bg-white px-8 py-6 text-center text-sm">
        <button type="button" class="text-brand font-semibold" @click="whenSignedIn(() => undefined)">
          {{ t("post.signInToComment") }}</button
        >{{ t("post.signInToCommentSuffix") }}
      </div>
    </aside>
  </div>
</template>
