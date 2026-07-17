# Wispr Terminal Popup — Implementation Plan

## Product intent

Create personal, Pixel 10 / Android 17-only software that lets a Quick Settings
tile open a beautiful, minimal dictation popup *over* Android Terminal. The popup
contains a real editable Android text field so Wispr Flow can dictate there.

The one complete interaction is:

```text
Android Terminal remains visible
  → tap “Dictate” Quick Settings tile
  → small translucent popup opens over Terminal and focuses its text field
  → dictate with Wispr Flow
  → Copy & return
  → popup disappears; Terminal is exactly where it was; paste into Pi
```

This is not a terminal integration, an accessibility service, a notification
workflow, or a floating-overlay app. Clipboard is intentionally the handoff.

## Hard constraints

- Target only the owner’s Pixel 10 on Android 17 (API 37): `minSdk =
  targetSdk = compileSdk = 37`.
- No backward-compatibility branches, device adaptation, analytics, network,
  accounts, storage permissions, notification permission, or background work.
- No launcher intent filter. The package should not appear as a conventional app
  in the launcher; Quick Settings is its entry point.
- The one unavoidable package icon is a restrained monochrome vector for the
  Quick Settings tile and Android Settings. There is no user-facing app icon,
  home-screen entry, splash experience, or main application screen.
- The terminal must remain visible beneath the popup. The popup must temporarily
  become the focused activity because a genuine focused text field is the point
  of the design.

## Chosen Android shape

### Components

| Component | Responsibility |
| --- | --- |
| `DictationTileService` | Declares the Quick Settings tile and launches the popup using the current TileService API. |
| `DictationActivity` | A no-history, translucent, non-launcher activity. Hosts the popup and finishes on dismissal or successful copy. |
| `DictationPopup` | Single Compose screen: text editor, close action, Copy, and primary Copy & return action. |
| `Clipboard` helper | Writes plain text to the system clipboard and shows no notification. |

The reference app demonstrated the important mechanism: a transparent,
translucent activity containing a focused dialog. Our implementation keeps that
mechanism but removes its navigation, persistence, notifications, permissions,
and full-app UI.

### Launch and return behavior

1. The user adds the tile once through Android’s Quick Settings editor.
2. Tapping the tile collapses the shade and starts `DictationActivity` from
   `DictationTileService` using the modern `PendingIntent` overload of
   `startActivityAndCollapse`.
3. The activity has a transparent window and a short fade/scale-in animation.
   It shows a compact dialog surface only; Android Terminal is visible around it.
4. Once the popup is on screen, its editor requests focus and invokes the IME.
   This deliberately gives Wispr Flow a normal editable Android target.
5. **Copy & return** copies nonblank text, performs a subtle confirmation, then
   finishes the activity. Back, outside-tap, and close finish without altering
   the clipboard.

There will be no attempt to infer when Wispr has finished dictating. The user
owns that moment by tapping Copy & return.

## Visual direction

The design should feel native to Android 17 rather than like a miniature
full-screen app.

- Use Jetpack Compose with Material 3 expressive primitives available in the
  installed Android 17 toolchain; do not imitate an older Material dialog.
- Dark-first, with an OLED-black/translucent scrim-free backdrop so the terminal
  retains visual presence.
- Surface: a near-black, lightly elevated rounded rectangle; generous radius;
  a very restrained cool-lilac or electric-blue focus accent. No gradients,
  illustrations, branding, headers, cards-inside-cards, or excess chrome.
- Editor: large, high-legibility type, multiline, minimal placeholder such as
  “Speak or type”. It is the visual center of gravity.
- Actions: icon-only close in the top corner; compact secondary **Copy**;
  prominent pill-shaped **Copy & return** with a clipboard/arrow treatment.
- Motion: brief, quiet scale + fade on entry and exit; respect the device’s
  animator scale automatically. No celebration animation or persistent toast.
- With the IME present, position and size the surface above it rather than
  covering the active terminal prompt unnecessarily.
- Let system dynamic color influence the accent only if it remains quiet and
  legible against the terminal; otherwise use the defined dark palette.

Before implementation, capture a single visual reference screenshot of the
owner’s Terminal + keyboard layout to tune popup width, vertical placement, and
contrast on the actual device.

## Data and interaction rules

- Start each invocation with an empty editor. This avoids accidentally pasting
  stale dictation into a terminal command.
- The text remains intact for the life of an open popup, including IME/layout
  changes and activity recreation via saved UI state.
- **Copy** copies text and leaves the popup open.
- **Copy & return** copies text and closes the popup. It is disabled for blank
  or whitespace-only content.
- Clipboard content is plain text only. No history, database, telemetry, or
  file persistence.
- Back, outside tap, and close discard the current in-popup text and do not
  change the clipboard.

## Build sequence

### 1. Bootstrap the smallest project

- Create a one-module Kotlin Android application using Gradle Kotlin DSL.
- Use the current stable Android Gradle Plugin/Kotlin/Compose versions supported
  by the locally installed API 37 SDK.
- Add only AndroidX Activity, Compose BOM, Material 3, and the necessary
  lifecycle tooling. Do not add DI, navigation, Room, Firebase, or a design
  system framework.
- Add a debug signing/install path for the personal device.

### 2. Wire the system entry point

- Declare the `TileService` with `BIND_QUICK_SETTINGS_TILE`, label it
  **Dictate**, and give it a small monochrome microphone/voice-wave tile icon.
- Declare a non-exported `DictationActivity` with no launcher category,
  translucent/transparent theme, no action bar, no preview window, and
  `noHistory` behavior.
- Ensure the tile is passive/stateless: it is an action, not a toggle.
- Verify that launching and closing it returns to the same Android Terminal
  task/session without appearing in Recents as a normal standalone task.

### 3. Implement the dictation popup

- Use a real Compose `TextField`/`BasicTextField` inside the focused activity;
  do not use a WindowManager overlay.
- Request focus after composition and explicitly request the keyboard using
  Compose’s current IME APIs.
- Build the three actions and clipboard behavior exactly as defined above.
- Handle window insets so the popup sits correctly when the keyboard and Wispr
  bubble are shown.
- Make every tappable control labelled and accessible, while keeping the visual
  UI sparse.

### 4. Tune against the physical device

- Install on the Pixel, add the tile once, and test from Android Terminal.
- Tune the popup’s width, elevation, vertical placement, focus delay, and IME
  timing based on the actual Wispr bubble—not an emulator.
- Confirm the translucent activity does not dim, snapshot, replace, or restart
  the terminal behind it.

### 5. Verify and package

- Run unit tests for blank-text action state and clipboard command behavior.
- Run Compose UI tests for initial focus intent, Copy, Copy & return, close, and
  state restoration.
- Build a release APK, install it on the owner’s device, then perform the
  end-to-end manual acceptance test below.

## Acceptance test

1. Open Android Terminal and place Pi at an editable prompt.
2. Open Quick Settings and tap **Dictate**.
3. The shade collapses. Terminal remains visibly unchanged behind a compact
   popup; its editor is focused and the keyboard appears.
4. Wispr Flow’s bubble appears for that editor.
5. Dictate several lines, edit them once, then tap **Copy & return**.
6. The popup closes without an app screen, terminal restart, notification, or
   unexpected task switch.
7. Paste into Pi. The pasted bytes exactly match the popup text.
8. Repeat with **Copy**, verify the popup stays open, then close it and confirm
   the copied text is still available.

## Explicit non-goals

- Automatic insertion into Terminal, key injection, Shizuku, ADB automation,
  terminal plugins, or guest-side changes.
- A launcher activity, settings screen, onboarding, account, billing, ads,
  telemetry, crash reporting, or cloud sync.
- Persistent floating buttons/overlays, notification reply fields, bubbles, or
  a notification-based copy flow.
- Supporting other phones, Android releases, launchers, keyboards, or dictation
  products.

## One gating check

The first device build is deliberately a thin but polished vertical slice. If
Wispr Flow does not appear in this standard focused popup field, stop before
adding features: the working assumption behind the bridge is false on the
device, and the next task becomes comparing that popup with the known-working
Push Note behavior.
