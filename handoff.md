# Wispr Flow × Android Terminal — Recon Handoff

## Goal

Restore practical Wispr Flow dictation while using Pi in Android Terminal on a Pixel 10 / Android 17. Do **not** start implementation yet; identify the actual Android-side trigger/constraint first.

## What the terminal actually is

This is Android's Linux Terminal architecture, not a native Android terminal widget:

```text
Android Terminal host app / WebView + xterm.js
  → VSOCK
  → Debian VM
  → socat → ttyd → PTY → bash → Pi TUI
```

Verified inside the guest:

- Debian 13 ARM64 VM; Crosvm virtual input devices; Android shared storage at `/mnt/shared`.
- `ttyd_uds.service` launches `ttyd` on a Unix socket; `socat` exposes VSOCK port 7681.
- Pi is only a Node.js terminal program at the end of the PTY chain. Its TUI creates ANSI output/cursor state, **not** an Android `EditText`, IME connection, accessibility node, or window.
- Pi raw mode / bracketed paste only affect terminal-byte handling after text reaches the guest. A Pi extension/theme/escape sequence cannot cause an Android overlay to appear.

Public architecture confirmation: Android’s Linux-terminal presentation describes `xterm.js (WebView)` + `ttyd (Guest)`:
https://lpc.events/event/19/contributions/2123/attachments/1730/3786/LPC%202025%20-%20A%20Linux%20VM%20on%20Android%20via%20AVF%20(Android%20MC).pdf

## Wispr facts from its current Android documentation

- Flow is an Android accessibility service plus a `Display over other apps` overlay.
- It says the Flow Bubble normally appears when the on-screen keyboard opens and hides when it closes.
- Accessibility is used to detect the focused target and insert transcription.
- It intentionally excludes password/numeric/phone fields and banking/financial apps.
- Its docs claim support for inline notification reply fields, including WhatsApp and Pixel/AOSP devices.

Sources:

- https://docs.wisprflow.ai/articles/5002934560-why-is-the-wispr-bar-is-not-appearing-or-disappearing
- https://docs.wisprflow.ai/articles/8858845757-setup-wispr-flow-on-android-android-settings

## Observations from the user (important)

All normal Flow permissions/settings had already been considered; Flow continues to work in ordinary Android app text fields.

| Context | On-screen keyboard | Flow bubble |
|---|---:|---:|
| Normal non-bubbled Android app text field | Yes | Yes |
| Android Terminal / Pi editor | Yes — user has been typing with it | No |
| Markdown/notes app opened as an Android Bubble | Yes | No |
| WhatsApp inline reply in notification shade | Yes | No |

The Terminal bubble **used to appear here** before the Android 17 Terminal update. It disappeared after that update.

## Current conclusion

Keyboard visibility alone is not sufficient on this device. The pattern indicates that Flow is selective about the **Android window/task/accessibility context**: it works in an ordinary foreground application activity but not in Android Terminal’s WebView-hosted terminal, Android Bubble tasks, or the tested SystemUI notification-reply context.

Most likely the Terminal update changed how its WebView/xterm input is exposed to Android accessibility/IME, or Flow now refuses that host/window context. This is Android Terminal ↔ Wispr behavior, not Pi behavior.

We cannot inspect Android packages, accessibility nodes, `dumpsys`, or logcat from this Debian VM. Those commands and Android private paths are unavailable here.

## Ideas evaluated

### 1. Pi/TUI hack — reject

Not viable. Pi has no host Android API surface and cannot create or alter the focused Android accessibility target.

### 2. Fork Android Terminal — technically direct but impractical

A host-app patch could potentially restore a normal editable accessibility/IME proxy for the WebView terminal, but it is a large Android/platform project with privileged VM and authenticated terminal plumbing. User considers this impractical.

### 3. Companion “dictation bridge” — possible, but unproven

Concept: a small Android app owns a genuine Android `EditText`; Wispr dictates there; the bridge forwards received text to Terminal.

Constraints:

- An ordinary app cannot impersonate/change Terminal’s accessibility tree for Wispr.
- A transparent Activity with a real `EditText` is more plausible than a pure overlay, but it temporarily owns focus.
- Automatically injecting the resulting text into Terminal requires a separate privileged/transport path (e.g. Shizuku/ADB, a guest-side receiver, or an accessibility action if Terminal accepts it). A normal app cannot freely inject keys into another app.
- Clipboard + manual paste is the simplest possible bridge MVP.

### 4. Notification `Reply` bridge — do not pursue yet

The proposed design was: persistent bridge notification → tap `Reply` → Wispr dictates into Android `RemoteInput` → bridge receives text → clipboard/forwarding.

Wispr’s docs claim notification-reply support, but the user tested WhatsApp inline reply in the shade and Flow did **not** appear. Therefore this route is low confidence on this Android 17 device and should not be built based on the documentation alone.

A persistent/ongoing notification cannot reliably be forced into a heads-up popup either; SystemUI decides that. A heads-up and an expanded-shade reply are different contexts, so there is no reason to assume a popup would fix the failed reply test.

## Best next recon

Perform Android-host-side comparison while an input is focused:

1. Capture/dump the accessibility tree for a working normal app field and for Android Terminal’s Pi editor.
2. Compare focused node properties: package/class, `editable`, `focusable`, password/input type, bounds, visibility, window/task type, and any WebView semantics.
3. If possible, gather Android Terminal and Wispr logcat around focus/keyboard events.
4. Repeat a minimal bridge experiment only after this: a standard foreground Activity with one real `EditText`, first proving Flow appears there, then testing a clipboard handoff.

The key question is no longer “how do we open the keyboard?” It is: **what exact focused-window/accessibility conditions cause Flow to decline its overlay on this device?**
