export default defineNuxtRouteMiddleware(() => {
  const { isSignedIn } = useSession();
  if (!isSignedIn.value) {
    useOverlays().open.value = "login";
    return navigateTo("/");
  }
});
