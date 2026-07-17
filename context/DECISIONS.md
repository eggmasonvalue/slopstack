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

(Superseded in part 2026-07-17: see "Add a launcher entry point" below — the
activity is now also reachable outside the tile, though the popup/clipboard
mechanism itself is unchanged.)

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

## 2026-07-17 — Adopt Material 3 Expressive on an alpha dependency, ahead of stable

Context: The owner wants the popup to track current Android/Pixel visual language
(wallpaper-adaptive dynamic color, Material 3 Expressive shapes/motion, the
bundled Pixel system font) rather than the static Material 3 look it shipped
with. As of this change, Material 3 Expressive's Compose APIs
(`MaterialExpressiveTheme`, `MotionScheme.expressive()`, `ButtonGroup`, the
shape-morphing `ButtonDefaults.shapes()`/`IconButtonDefaults.shapes()`
overloads) are marked `internal` in the latest stable Compose Material3 (1.4.0)
and only became usable in the `1.5.0-alpha` line; Google's own roadmap places
stable promotion "later in 2026."

Decision: Pin `androidx.compose.material3:material3` to `1.5.0-alpha24`
(overriding the Compose BOM's stable pick) and use the experimental Expressive
theme/shape/motion/`ButtonGroup` APIs now, gated behind
`@OptIn(ExperimentalMaterial3ExpressiveApi::class)`. This in turn required
bumping AGP to 9.3.0 (built-in Kotlin support replaces the separate
`org.jetbrains.kotlin.android` plugin), Gradle to 9.5.0, and Kotlin to 2.4.10,
since the alpha material3 artifact's transitive Compose UI/Foundation/Animation
requirements exceed what the previous toolchain could resolve.

Tradeoff: The app depends on a pre-release library on its only visual layer.
Alpha Expressive APIs can still change shape or be renamed before 1.5.0 stable;
upgrading again later may require touching `DictationActivity.kt`. This is
accepted here because the app is personal, single-device software where
visual currency ranks above dependency conservatism — a tradeoff that would
not be appropriate for software with other users or release obligations.

Status: active

## 2026-07-17 — Bundle Google Sans Flex as a font resource, not via downloadable-fonts API

Context: Google open-sourced Google Sans Flex (OFL-1.1) in November 2025. The
owner wants the Pixel system font on the popup's text. Compose offers a
downloadable-fonts API (`GoogleFont.Provider`) that fetches fonts at runtime
through Google Play Services, but this project has a hard no-network
constraint (see "Restrict the app to Pixel 10 on Android 17" above and
`context/CONVENTIONS.md`).

Decision: Download the `.ttf` once and bundle it directly under
`app/src/main/res/font/`, referenced through a plain Compose `FontFamily`. No
network call, Play Services dependency, or font-provider certificate check is
added.

Tradeoff: The font is frozen at the bundled version and won't receive upstream
fixes automatically; this is preferred here over adding a runtime dependency
and network path to an otherwise fully offline app. The font isn't yet in the
public `google/fonts` repo, so `licenses/google-sans-flex-NOTICE.txt` carries a
generic OFL notice rather than the family's exact upstream copyright line;
replace it with the official `OFL.txt` once upstream publishes one.

Status: active

## 2026-07-17 — Distribute signed release APKs via tag-triggered GitHub Actions

Context: The owner wants an installable APK downloadable from any device
(browser sideload) without needing a local Android toolchain everywhere, and
expects infrequent updates. Android also requires every update to an installed
package to be signed by the same key, or the previous install must be removed
first.

Decision: Two workflows split the concerns:

- `.github/workflows/debug-build.yml` builds an unsigned debug APK on every
  push to `main` and attaches it as a workflow artifact — a convenience build,
  not a distribution channel.
- `.github/workflows/release.yml` builds a signed release APK only when a
  `v*.*.*` tag is pushed, deriving `versionName`/`versionCode` from the tag and
  run number, and publishes it to a GitHub Release.

A dedicated release keystore (`wispr-dictate-release`, 30-year validity) was
generated once and stored only as GitHub Actions secrets
(`RELEASE_KEYSTORE_BASE64`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`,
`RELEASE_KEY_PASSWORD`); it is never committed. `app/build.gradle.kts`'s release
`signingConfig` reads these from environment variables and is a no-op (unsigned
build) when they're absent, so local `assembleRelease` still works without the
key.

Tradeoff: Losing the keystore means every future signed release requires
uninstalling the previous install first, since there's no key-recovery
mechanism (no Play App Signing, no secondary custodian) — acceptable here only
because this is single-owner, single-device software; a local backup copy is
kept outside the repository as the sole recovery path.

Status: active

## 2026-07-17 — Add a launcher entry point for Quick Tap / Assistant

Context: The original design deliberately gave `DictationActivity` no launcher
category so the app wouldn't appear as a conventional app (see "Restrict the
app to Pixel 10" and the app's original non-goals). The owner later wanted
Pixel's Quick Tap (double-tap-back) gesture to open the popup. Quick Tap's
"open app" picker — like Assistant's "open the app" voice command and the
long-press app-shortcut menu — only lists apps with a `MAIN`/`LAUNCHER`
activity; none of them require or use an `AccessibilityService`, which had been
the rejected alternative (see the popup/clipboard decision above).

Decision: Add a `MAIN`/`LAUNCHER` intent-filter to `DictationActivity` and mark
it `exported="true"` (required for the launcher/Quick Tap/Assistant, all
external processes, to start it). `excludeFromRecents` and `noHistory` are
unchanged, so tapping the drawer icon opens and closes the same transient
popup as the Quick Settings tile — no separate main screen, splash, or
navigation was added.

Tradeoff: The app now has a visible icon in the app drawer, reversing the
original "no user-facing app icon" constraint. This was a deliberate,
explicit reversal by the owner, not a default; an `AccessibilityService`-based
shortcut was considered and rejected first because Android's Restricted
Settings flow and OEM battery-optimization kills make sideloaded accessibility
services unreliable, whereas a launcher activity has no such failure modes.

Status: active
