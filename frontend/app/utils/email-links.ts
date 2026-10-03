export type TextSegment = { kind: "text"; text: string } | { kind: "email"; email: string };

const EMAIL = /[\w.+-]+@[\w.-]+\w/g;

export function splitEmails(text: string): TextSegment[] {
  const segments: TextSegment[] = [];
  let last = 0;
  for (const match of text.matchAll(EMAIL)) {
    const start = match.index;
    if (start > last) {
      segments.push({ kind: "text", text: text.slice(last, start) });
    }
    segments.push({ kind: "email", email: match[0] });
    last = start + match[0].length;
  }
  if (last < text.length) {
    segments.push({ kind: "text", text: text.slice(last) });
  }
  return segments;
}
