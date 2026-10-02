export default defineNuxtPlugin(async () => {
  const { messages } = useMessages();
  const { locale } = useLocale();
  if (Object.keys(messages.value).length === 0) {
    messages.value = await useRequestFetch()<Record<string, string>>(`/api/messages?lang=${locale.value}`);
  }
});
