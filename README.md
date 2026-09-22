# Monexo Android App

Monexo is a native Android WebView application configured for [https://monexo.wiki](https://monexo.wiki).

## Features
- **Clean Fullscreen**: Headerless layout with immersive edge-to-edge browsing.
- **Status Bar Integration**: Deep black status bar header matching Android system indicators.
- **Custom Splash Screen**: Monexo branded blue splash screen with animated transition.
- **Permissions**: Notification (`POST_NOTIFICATIONS`), SMS (`RECEIVE_SMS`, `READ_SMS`), and Storage permission requests.
- **Offline & Error Resilience**: Offline detection with Retry and Home navigation affordances.

## How to Build APK with GitHub Actions
1. Push code to this repository (or trigger manually under **Actions** tab -> **Build and Release APK** -> **Run workflow**).
2. Once the workflow run completes, download the generated APK from the **Artifacts** section at the bottom of the workflow summary page.

## Building Locally
Ensure JDK 21+ and Android SDK are installed, then run:
```bash
./gradlew assembleDebug
```
The resulting APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`
