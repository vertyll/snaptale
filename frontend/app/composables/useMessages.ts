import { IntlMessageFormat } from "intl-messageformat";
import { messageOf } from "~/utils/api-error";
import type { Message, MessageArgs } from "~/utils/types";

const LOCALE = "pl";
const cache = new Map<string, IntlMessageFormat>();

export function useMessages() {
  const messages = useState<Record<string, string>>("messages", () => ({}));

  function t(code: string, args: MessageArgs = {}): string {
    const text = messages.value[code] ?? messages.value["errors.unknown"] ?? code;
    let format = cache.get(text);
    if (!format) {
      format = new IntlMessageFormat(text, LOCALE);
      cache.set(text, format);
    }
    return String(format.format(args));
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
