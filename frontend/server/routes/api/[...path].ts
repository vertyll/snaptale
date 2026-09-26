export default defineEventHandler((event) =>
  proxyRequest(event, backendUrl(event.path, useRuntimeConfig(event).backendInternalUrl))
);
