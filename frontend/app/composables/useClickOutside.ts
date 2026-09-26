import type { Ref } from "vue";

export function useClickOutside(target: Readonly<Ref<HTMLElement | null>>, handler: () => void): void {
  function listener(event: MouseEvent) {
    if (target.value && !target.value.contains(event.target as Node)) {
      handler();
    }
  }
  onMounted(() => document.addEventListener("mouseup", listener));
  onBeforeUnmount(() => document.removeEventListener("mouseup", listener));
}
