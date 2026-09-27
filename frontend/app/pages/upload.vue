<script setup lang="ts">
import type { PostCard } from "~/utils/types";

const { t } = useMessages();
definePageMeta({ layout: "upload", middleware: "auth" });

const TEXT_MAX = 300;
const MAX_MEGABYTES = 100;

const api = useApi();
const { me } = useSession();
const { submit, pending, fieldError, formError } = useForm();
const file = ref<File | null>(null);
const preview = ref<string | null>(null);
const text = ref("");
const localError = ref<string | null>(null);

function choose(picked: File | undefined) {
  localError.value = null;
  if (!picked) {
    return;
  }
  if (picked.type !== "video/mp4") {
    localError.value = t("upload.onlyMp4");
    return;
  }
  if (picked.size > MAX_MEGABYTES * 1024 * 1024) {
    localError.value = t("upload.tooLarge", { max: MAX_MEGABYTES });
    return;
  }
  clearVideo();
  file.value = picked;
  preview.value = URL.createObjectURL(picked);
}

function clearVideo() {
  if (preview.value) {
    URL.revokeObjectURL(preview.value);
  }
  file.value = null;
  preview.value = null;
}

function discard() {
  clearVideo();
  text.value = "";
  localError.value = null;
}

async function publish() {
  const data = new FormData();
  if (file.value) {
    data.append("video", file.value);
  }
  data.append("text", text.value);
  const ok = await submit(() => api.post<PostCard>("/api/posts", data));
  if (ok && me.value) {
    discard();
    await navigateTo(`/profile/${me.value.id}`);
  }
}

onBeforeUnmount(clearVideo);
</script>

<template>
  <div class="w-full">
    <output
      v-if="pending"
      class="fixed top-0 left-0 z-50 flex h-screen w-full items-center justify-center bg-black/50"
      :aria-label="t('upload.uploading')"
    >
      <Icon name="mdi:loading" size="100" class="animate-spin text-white" />
    </output>
    <main class="mt-[80px] mb-[40px] w-full rounded-md bg-white px-4 py-6 shadow-lg md:px-10">
      <h1 class="text-[23px] font-semibold">{{ t("upload.title") }}</h1>
      <p class="mt-1 text-gray-400">{{ t("upload.subtitle") }}</p>

      <div class="mt-8 gap-6 md:flex">
        <label
          v-if="!preview"
          class="mx-auto mt-4 mb-6 flex h-[470px] w-full max-w-[260px] cursor-pointer flex-col items-center justify-center rounded-lg border-2 border-dashed border-gray-300 p-3 text-center hover:bg-gray-100 md:mx-0"
          @drop.prevent="choose($event.dataTransfer?.files[0])"
          @dragover.prevent
        >
          <Icon name="mdi:cloud-upload" size="40" class="text-gray-400" />
          <span class="mt-4 text-[17px]">{{ t("upload.choose") }}</span>
          <span class="mt-1.5 text-[13px] text-gray-500">{{ t("upload.drop") }}</span>
          <span class="mt-12 text-sm text-gray-400">MP4</span>
          <span class="mt-2 text-[13px] text-gray-400">{{ t("upload.limit", { max: MAX_MEGABYTES }) }}</span>
          <span class="bg-brand mt-8 w-[80%] rounded-sm px-2 py-1.5 text-[15px] text-white">{{
            t("upload.pick")
          }}</span>
          <input
            type="file"
            class="hidden"
            accept="video/mp4"
            @change="choose(($event.target as HTMLInputElement).files?.[0])"
          />
        </label>
        <div
          v-else
          class="relative mx-auto mt-4 mb-16 flex h-[540px] w-full max-w-[260px] items-center justify-center p-3 md:mx-0 md:mb-12"
        >
          <div class="h-full w-full bg-black" />
          <img
            class="pointer-events-none absolute z-20"
            src="~/assets/images/mobile-case.png"
            alt=""
            aria-hidden="true"
          />
          <video
            :src="preview"
            autoplay
            loop
            muted
            playsinline
            class="absolute z-10 h-full w-full rounded-xl object-cover p-[13px]"
          />
          <div
            class="absolute -bottom-12 z-50 flex w-full items-center justify-between rounded-xl border border-gray-300 p-2"
          >
            <div class="flex items-center truncate text-[11px]">
              <Icon name="mdi:check-circle-outline" size="16" class="min-w-[16px]" />
              <span class="truncate pl-1">{{ file?.name }}</span>
            </div>
            <button type="button" class="ml-2 text-[11px] font-semibold" @click="clearVideo">
              {{ t("upload.change") }}
            </button>
          </div>
        </div>

        <form class="mt-4 mb-6 w-full" @submit.prevent="publish">
          <div class="mt-5">
            <div class="flex items-center justify-between">
              <label for="caption" class="mb-1 text-[15px]">{{ t("upload.caption") }}</label>
              <span class="text-[12px] text-gray-400">{{ text.length }}/{{ TEXT_MAX }}</span>
            </div>
            <input
              id="caption"
              v-model="text"
              :maxlength="TEXT_MAX"
              type="text"
              class="w-full rounded-md border border-gray-300 p-2.5 focus:outline-none"
            />
            <span v-if="fieldError('text')" class="text-[14px] font-semibold text-red-500">{{
              fieldError("text")
            }}</span>
          </div>
          <p v-if="localError || formError" class="mt-4 font-semibold text-red-600" role="alert">
            {{ localError ?? formError }}
          </p>
          <div class="flex gap-3">
            <button
              type="button"
              class="mt-8 rounded-sm border border-gray-300 px-10 py-2.5 hover:bg-gray-100"
              @click="discard"
            >
              {{ t("upload.clear") }}
            </button>
            <button
              type="submit"
              :disabled="!file || !text.trim() || pending"
              class="bg-brand mt-8 rounded-sm px-10 py-2.5 text-white disabled:bg-gray-200"
            >
              {{ t("common.publish") }}
            </button>
          </div>
        </form>
      </div>
    </main>
  </div>
</template>
