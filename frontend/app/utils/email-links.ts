export type TextSegment = { kind: "text"; text: string; at: number } | { kind: "email"; email: string; at: number };

const LOCAL_PART = /[\w.+-]/;
const DOMAIN = /[\w.-]/;
const WORD = /\w/;

function emailAround(text: string, at: number, from: number): { start: number; end: number } | undefined {
  let start = at;
  while (start > from && LOCAL_PART.test(text.charAt(start - 1))) {
    start--;
  }
  let end = at + 1;
  while (end < text.length && DOMAIN.test(text.charAt(end))) {
    end++;
  }
  while (end > at + 1 && !WORD.test(text.charAt(end - 1))) {
    end--;
  }
  return start < at && end > at + 1 ? { start, end } : undefined;
}

export function splitEmails(text: string): TextSegment[] {
  const segments: TextSegment[] = [];
  let last = 0;
  for (let at = text.indexOf("@"); at !== -1; at = text.indexOf("@", Math.max(at + 1, last))) {
    const email = emailAround(text, at, last);
    if (email === undefined) {
      continue;
    }
    if (email.start > last) {
      segments.push({ kind: "text", text: text.slice(last, email.start), at: last });
    }
    segments.push({ kind: "email", email: text.slice(email.start, email.end), at: email.start });
    last = email.end;
  }
  if (last < text.length) {
    segments.push({ kind: "text", text: text.slice(last), at: last });
  }
  return segments;
}
