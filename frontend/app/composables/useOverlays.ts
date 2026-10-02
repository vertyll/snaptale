export type Overlay = "editProfile";

export function useOverlays() {
  const open = useState<Overlay | null>("overlay", () => null);
  const { isSignedIn, signIn } = useSession();

  function whenSignedIn(action: () => unknown): void {
    if (isSignedIn.value) {
      action();
    } else {
      void signIn();
    }
  }

  return { open, whenSignedIn, close: () => (open.value = null) };
}
