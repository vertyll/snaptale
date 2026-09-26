export type Overlay = "login" | "register" | "forgotPassword" | "editProfile";

export function useOverlays() {
  const open = useState<Overlay | null>("overlay", () => null);
  const { isSignedIn } = useSession();

  function whenSignedIn(action: () => unknown): void {
    if (isSignedIn.value) {
      action();
    } else {
      open.value = "login";
    }
  }

  return { open, whenSignedIn, close: () => (open.value = null) };
}
