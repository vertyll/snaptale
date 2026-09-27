import { ApiError } from "~/utils/api-error";

export function useForm() {
  const { t, message } = useMessages();
  const error = ref<unknown>(null);
  const pending = ref(false);

  async function submit(action: () => Promise<unknown>): Promise<boolean> {
    pending.value = true;
    error.value = null;
    try {
      await action();
      return true;
    } catch (error_: unknown) {
      error.value = error_;
      return false;
    } finally {
      pending.value = false;
    }
  }

  function fieldError(name: string): string | undefined {
    const field = error.value instanceof ApiError ? error.value.field(name) : undefined;
    return field ? message(field) : undefined;
  }

  const formError = computed(() => {
    if (!error.value) {
      return undefined;
    }
    if (error.value instanceof ApiError && error.value.problem?.errors) {
      return undefined;
    }
    return error.value instanceof ApiError ? message(error.value.summary) : t("errors.unknown");
  });

  return { submit, pending, fieldError, formError, error };
}
