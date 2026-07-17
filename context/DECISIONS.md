# Decisions

Use this file for durable, non-obvious project decisions. It is not a changelog,
status list, or implementation journal.

## 2026-07-17 — Use a focused popup and clipboard handoff

Context: Android Terminal's WebView-backed terminal does not expose a suitable
editable Android target for the dictation product, while ordinary focused Android
text fields do.

Decision: Launch a transparent, no-history activity from a Quick Settings tile,
collect dictation in its real text editor, and hand plain text to Android Terminal
through the system clipboard.

Tradeoff: The popup temporarily owns focus and the user must paste manually;
terminal injection, overlays, accessibility automation, and guest-side changes
are excluded.

Status: active

## 2026-07-17 — Restrict the app to Pixel 10 on Android 17

Context: This is personal software intended for one known device and OS release.

Decision: Set the Android minimum, target, and compile SDK levels to API 37 and
do not add compatibility behavior for other devices or releases.

Tradeoff: The app is intentionally not portable, avoiding compatibility code and
test coverage for unsupported environments.

Status: active
