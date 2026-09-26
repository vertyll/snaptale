export default defineNuxtPlugin(async () => {
  const { messages } = useMessages();
  if (Object.keys(messages.value).length === 0) {
    messages.value = await useRequestFetch()<Record<string, string>>("/api/messages");
  }
});
