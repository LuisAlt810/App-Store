# App-Store

App-Store is a lightweight Store-inspired Android app store with a fake 50-app catalog, installed app detection, update checks, permission UX, battery optimization controls, and foreground background service support.

## What is included
- Material 3 Compose UI with Home, Search, Updates, Installed, Downloads, and Settings screens
- 50 sample apps in the in-memory fake source
- Installed app detection using the Android package manager
- Update simulation for apps already installed on the device
- Notification channel setup
- Foreground service declaration for data-sync downloads
- Battery optimization toggle via `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`
- App icon and Android manifest with the required permissions

## Required permissions
- `POST_NOTIFICATIONS`
- `REQUEST_INSTALL_PACKAGES`
- `QUERY_ALL_PACKAGES`
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_DATA_SYNC`
- `INTERNET`

## Run it
1. Install Android Studio or the Android SDK Platform 35 and Build-Tools 35.
2. Ensure `local.properties` points to your SDK directory.
3. Run:

```bash
./gradlew assembleDebug
```

4. Install the debug APK on an emulator or physical device.

## Legal & safety
- Only distribute apps you have permission to distribute or that are explicitly open-source and licensable.
- `QUERY_ALL_PACKAGES` is requested to detect installed apps and show update state; this is a trade-off that may be restricted by Play policy outside sideloaded builds.
- `REQUEST_INSTALL_PACKAGES` is necessary to use the system package installer for APK installs and updates.
- Do not install APKs that fail checksum verification or come from untrusted sources.

## Notes
- The app uses a fake in-memory source by default so the UI can be built and tested before swapping in an external backend.
- This is intentionally source-agnostic and designed to accept a real repository backend later without changing the app UI.
