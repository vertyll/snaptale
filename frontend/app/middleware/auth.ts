export default defineNuxtRouteMiddleware(() => {
  const { isSignedIn, signIn } = useSession();
  if (!isSignedIn.value) {
    return signIn();
  }
});
