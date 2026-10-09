# Error responses

What the back-end answers when it refuses a request, and how the front-end turns that into text.

The back-end never sends a sentence a person reads. Every refusal is an RFC 9457 problem document
(`application/problem+json`, `common/ApiExceptionHandler`):

| Field    | Holds                                                                                      |
|----------|--------------------------------------------------------------------------------------------|
| `status` | the HTTP status                                                                            |
| `code`   | a key of the message catalog; a refusal Spring raises itself gets `errors.status.{status}` |
| `args`   | the ICU arguments for that key                                                             |
| `errors` | in a validation error, one `{ code, args }` per invalid field                              |

The catalog is the back-end's: it ships in `backend/src/main/resources/messages` (`pl.json`, `en.json`, ICU
MessageFormat) and `GET /api/messages?lang=pl|en` serves it, cached for an hour. It is not editable at runtime.

The front-end renders it. `useMessages` loads the catalog for the language in the `lang` cookie and formats a key with
its arguments through `IntlMessageFormat`. A failed call is an `ApiError` (`utils/api-error.ts`): `summary` is the
problem's `{ code, args }`, `field(name)` the message for one input, and a response without a problem document becomes
`errors.status.{status}`. The same catalog holds every label, so a new error or a new label is a new key in both
files, never a sentence in the code.
