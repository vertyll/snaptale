export type Overlay = "editProfile";

export function useOverlays() {
  const open = useState<Overlay | null>("overlay", () => null);
  const { isSignedIn, signIn } = useSession();
  const route = useRoute();

  function whenSignedIn(action: () => unknown): unknown {
    return isSignedIn.value ? action() : signIn(route.fullPath);
  }

  return { open, whenSignedIn, close: () => (open.value = null) };
}
