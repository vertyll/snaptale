import type { Me } from "~/utils/types";

// Reads who is signed in before the first route's middleware runs, so a page that needs a session sees the reader's
// on the server as well as in the browser.
export default defineNuxtPlugin(async () => {
  const { me } = useSession();
  const requestFetch = useRequestFetch();
  await callOnce("session", async () => {
    me.value = (await requestFetch<{ user: Me | null }>("/api/me")).user;
  });
});
