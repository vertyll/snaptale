import { IntlMessageFormat } from "intl-messageformat";
import { messageOf } from "~/utils/api-error";
import type { Message, MessageArgs } from "~/utils/types";

const cache = new Map<string, IntlMessageFormat>();

export function useMessages() {
  const messages = useState<Record<string, string>>("messages", () => ({}));
  const locale = useCookie<string>("lang", { default: () => "pl" });

  function t(code: string, args: MessageArgs = {}): string {
    const text = messages.value[code];
    if (text === undefined) {
      console.error(`Missing translation: ${code}`);
      return code;
    }
    try {
      const cacheKey = `${locale.value}:${text}`;
      let format = cache.get(cacheKey);
      if (!format) {
        format = new IntlMessageFormat(text, locale.value);
        cache.set(cacheKey, format);
      }
      return String(format.format(args));
    } catch (error) {
      console.error(`Invalid translation: ${code}`, error);
      return code;
    }
  }

  return {
    messages,
    t,
    message: (message: Message) => t(message.code, message.args),
    errorText: (error: unknown) => {
      const message = messageOf(error);
      return t(message.code, message.args);
    },
  };
}
