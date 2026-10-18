export default defineNuxtRouteMiddleware((to) => {
  const { isSignedIn, signIn } = useSession();
  if (!isSignedIn.value) {
    return signIn(to.fullPath);
  }
});
