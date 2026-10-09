# Media storage

Where uploaded files are written, and how they reach the browser.

Videos (up to 100 MB) and avatars (up to 5 MB, cropped to 512 px) are written to the media directory and served from
`/media/**` with a one-year cache, since a stored file never changes: a new upload gets a new path. A file whose row is
deleted is removed once the transaction commits, and a file stored for a transaction that rolls back is removed too.
