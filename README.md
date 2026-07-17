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

Install Android SDK Platform 37 (`platforms;android-37.0`) and build-tools 37.0.0,
and use JDK 17. From a machine with Gradle available:

```sh
gradle :app:assembleDebug
gradle :app:installDebug
```

A Gradle wrapper is checked in; `./gradlew` works the same way once
`local.properties` points `sdk.dir` at an SDK containing Platform 37.

Add the resulting **Dictate** tile through Android's Quick Settings editor.

## Documentation checks

```sh
npm install
npm run lint:md
npm run format:md
```

Project navigation and durable design context live in [`AGENTS.md`](AGENTS.md)
and [`context/`](context/).
