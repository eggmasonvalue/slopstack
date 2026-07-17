# Wispr Terminal Dictation Bridge

A personal Android companion for Pixel 10 on Android 17. Its Quick Settings
**Dictate** tile opens a compact focused text field above Android Terminal so
Wispr Flow can dictate into a normal Android editor. **Copy & return** puts the
text on the system clipboard and closes the popup; paste it into the terminal.

The project is a single Android application module. The implementation plan and
product constraints are in [`PLAN.md`](PLAN.md).

## Intended use

1. Open Android Terminal at an editable prompt.
2. Open Quick Settings and tap **Dictate**.
3. Dictate or type in the popup.
4. Tap **Copy & return** and paste into Terminal.

## Build

Install Android SDK Platform 37 and use a JDK compatible with the Android Gradle
Plugin. From a machine with Gradle available:

```sh
gradle :app:assembleDebug
gradle :app:installDebug
```

Add the resulting **Dictate** tile through Android's Quick Settings editor.

## Documentation checks

```sh
npm install
npm run lint:md
npm run format:md
```

Project navigation and durable design context live in [`AGENTS.md`](AGENTS.md)
and [`context/`](context/).
