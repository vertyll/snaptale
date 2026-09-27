<script setup lang="ts">
import { CircleStencil, Cropper } from "vue-advanced-cropper";
import "vue-advanced-cropper/dist/style.css";

const { t } = useMessages();
const NAME_MAX = 50;
const BIO_MAX = 160;

const { me, refresh } = useSession();
const { close } = useOverlays();
const dialog = ref<HTMLDialogElement | null>(null);

onMounted(() => dialog.value?.showModal());
const api = useApi();
const profileForm = useForm();
const avatarForm = useForm();

const name = ref(me.value?.name ?? "");
const bio = ref(me.value?.bio ?? "");
const file = ref<File | null>(null);
const preview = ref<string | null>(null);
const cropper = useTemplateRef<InstanceType<typeof Cropper>>("cropper");

const changed = computed(() => {
  const current = me.value;
  if (name.value.trim() === "") {
    return false;
  }
  return !current || name.value !== current.name || bio.value !== (current.bio ?? "");
});

function pickImage(event: Event) {
  const picked = (event.target as HTMLInputElement).files?.[0];
  if (picked) {
    file.value = picked;
    preview.value = URL.createObjectURL(picked);
  }
}

function cancelCrop() {
  if (preview.value) {
    URL.revokeObjectURL(preview.value);
  }
  preview.value = null;
  file.value = null;
}

async function saveAvatar() {
  const coordinates = cropper.value?.getResult().coordinates;
  if (!file.value || !coordinates) {
    return;
  }
  const data = new FormData();
  data.append("image", file.value);
  data.append("x", String(Math.round(coordinates.left)));
  data.append("y", String(Math.round(coordinates.top)));
  data.append("width", String(Math.round(coordinates.width)));
  data.append("height", String(Math.round(coordinates.height)));
  const ok = await avatarForm.submit(async () => {
    await api.put("/api/me/avatar", data);
    await refresh();
  });
  if (ok) {
    cancelCrop();
    await refreshNuxtData();
  }
}

async function saveProfile() {
  const ok = await profileForm.submit(async () => {
    await api.patch("/api/me", { name: name.value, bio: bio.value });
    await refresh();
  });
  if (ok) {
    close();
    await refreshNuxtData();
  }
}
</script>

<template>
  <dialog
    ref="dialog"
    class="fixed inset-0 z-50 m-0 h-full max-h-none w-full max-w-none justify-center overflow-auto border-0 bg-black/50 p-0 pt-14 backdrop:bg-transparent open:flex md:pt-[105px]"
    aria-labelledby="edit-profile-title"
    @cancel.prevent="close"
  >
    <div class="relative mx-3 mb-10 h-fit w-full max-w-[700px] rounded-lg bg-white">
      <div class="flex items-center justify-between border-b border-gray-300 p-5">
        <h2 id="edit-profile-title" class="text-[22px] font-medium">{{ t("profile.edit") }}</h2>
        <button type="button" :aria-label="t('common.close')" @click="close">
          <Icon name="mdi:close" size="25" />
        </button>
      </div>

      <div v-if="preview" class="p-4">
        <Cropper ref="cropper" class="h-[430px]" :src="preview" :stencil-component="CircleStencil" />
        <p v-if="avatarForm.formError.value" class="pt-2 text-[14px] font-semibold text-red-500" role="alert">
          {{ avatarForm.formError.value }}
        </p>
        <div class="flex justify-end gap-3 pt-4">
          <button
            type="button"
            class="rounded-sm border border-gray-300 px-5 py-[6px] hover:bg-gray-100"
            @click="cancelCrop"
          >
            {{ t("common.cancel") }}
          </button>
          <button
            type="button"
            class="bg-brand rounded-md px-7 py-[6px] font-medium text-white disabled:bg-gray-200"
            :disabled="avatarForm.pending.value"
            @click="saveAvatar"
          >
            {{ t("profile.apply") }}
          </button>
        </div>
      </div>

      <form v-else class="p-4" @submit.prevent="saveProfile">
        <section class="flex flex-col border-b border-gray-200 py-3 sm:flex-row">
          <div class="mb-2 font-semibold text-gray-700 sm:w-[160px]">{{ t("profile.avatar") }}</div>
          <label class="relative mx-auto cursor-pointer" :aria-label="t('profile.changeAvatar')">
            <UserAvatar :url="me?.avatarUrl ?? null" :size="95" :alt="me?.name ?? ''" />
            <span class="absolute right-0 bottom-0 rounded-full border border-gray-300 bg-white p-1 shadow-xl">
              <Icon name="mdi:pencil" size="17" />
            </span>
            <input class="hidden" type="file" accept="image/png, image/jpeg" @change="pickImage" />
          </label>
        </section>

        <section class="flex flex-col border-b border-gray-200 py-3 sm:flex-row">
          <div class="mb-2 font-semibold text-gray-700 sm:w-[160px]">{{ t("profile.name") }}</div>
          <div class="mx-auto w-full max-w-md sm:w-[60%]">
            <TextField
              v-model="name"
              :label="t('profile.username')"
              :maxlength="NAME_MAX"
              :error="profileForm.fieldError('name')"
            />
          </div>
        </section>

        <section class="flex flex-col py-3 sm:flex-row">
          <label for="bio" class="mb-2 font-semibold text-gray-700 sm:w-[160px]">{{ t("profile.bio") }}</label>
          <div class="mx-auto w-full max-w-md sm:w-[60%]">
            <textarea
              id="bio"
              v-model="bio"
              rows="4"
              :maxlength="BIO_MAX"
              class="bg-surface w-full resize-none rounded-md border border-gray-300 px-3 py-2.5 text-gray-800 focus:outline-none"
            />
            <div class="text-[11px] text-gray-500">{{ bio.length }}/{{ BIO_MAX }}</div>
            <span v-if="profileForm.fieldError('bio')" class="text-[14px] font-semibold text-red-500">
              {{ profileForm.fieldError("bio") }}
            </span>
          </div>
        </section>

        <p v-if="profileForm.formError.value" class="text-[14px] font-semibold text-red-500" role="alert">
          {{ profileForm.formError.value }}
        </p>
        <div class="flex justify-end gap-3 border-t border-gray-300 pt-4">
          <button
            type="button"
            class="rounded-sm border border-gray-300 px-5 py-[6px] hover:bg-gray-100"
            @click="close"
          >
            {{ t("common.cancel") }}
          </button>
          <button
            type="submit"
            class="bg-brand rounded-md px-7 py-[6px] font-medium text-white disabled:bg-gray-200"
            :disabled="!changed || profileForm.pending.value"
          >
            {{ t("common.save") }}
          </button>
        </div>
      </form>
    </div>
  </dialog>
</template>
