# Conventions

- Keep the Android app single-module unless a new module has a clear ownership boundary.
- Keep the Quick Settings tile stateless.
- Start every dictation invocation with an empty editor.
- Keep clipboard writes plain text only.
- Keep user text in saved UI state only; do not persist it.
- Do not add network calls, analytics, telemetry, crash reporting, or cloud sync.
- Do not add accounts, billing, storage permissions, or notification permission.
- Do not add background services, WorkManager jobs, or other work that runs
  without a direct user tap.
- Do not add backward-compatibility branches or device adaptation; this app
  targets only the owner's Pixel 10 on Android 17 (API 37).
- Run `npm run lint:md` before submitting documentation changes.
- Run `npm run format:md` to format Markdown.
- Run `gradle :app:lint :app:test` before submitting Android changes.
- Run `gradle :app:connectedAndroidTest` on a device before submitting UI changes.
