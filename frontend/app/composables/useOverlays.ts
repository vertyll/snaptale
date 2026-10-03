export type Overlay = "editProfile";

export function useOverlays() {
  const open = useState<Overlay | null>("overlay", () => null);
  const { isSignedIn, signIn } = useSession();

  function whenSignedIn(action: () => unknown): unknown {
    return isSignedIn.value ? action() : signIn();
  }

  return { open, whenSignedIn, close: () => (open.value = null) };
}
