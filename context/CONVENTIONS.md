# Conventions

- Keep the Android app single-module unless a new module has a clear ownership boundary.
- Keep the Quick Settings tile stateless.
- Start every dictation invocation with an empty editor.
- Keep clipboard writes plain text only.
- Keep user text in saved UI state only; do not persist it.
- Run `npm run lint:md` before submitting documentation changes.
- Run `npm run format:md` to format Markdown.
- Run `gradle :app:lint :app:test` before submitting Android changes.
- Run `gradle :app:connectedAndroidTest` on a device before submitting UI changes.
