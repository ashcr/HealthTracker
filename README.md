# Health Tracker (Android)

A native Android app with three features:
1. **Migraine Tracker** — log episode start/end, duration is auto-calculated, triggers, what helped, severity, and history with monthly count.
2. **Water Intake** — 2L/day goal, quick-add buttons, reminder notifications every 2 hours (8am–10pm).
3. **Medication Reminder** — Cirtel 40, every 12 hours, notification with "Mark as Taken" button, tracks missed doses.

## How to build the APK from your phone (no laptop, no Android Studio)

This project can't be compiled inside this chat sandbox (no internet access to Google's Maven repo here), but **GitHub Actions will build it for you automatically, for free**, the moment you push it to a repo. Here's how, entirely from your phone:

### Step 1 — Create a GitHub repo
1. Open github.com in your phone browser (or the GitHub app) and sign in / sign up.
2. Tap **New repository**, name it `HealthTracker`, keep it Public or Private, and create it (don't add a README).

### Step 2 — Upload this project
Easiest from a phone: use GitHub's web uploader.
1. On your new repo page, tap **Add file → Upload files**.
2. Upload all the files/folders from this project, keeping the folder structure intact (`app/`, `.github/`, `gradle/`, `gradlew`, `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`).
3. Commit directly to `main`.

*(If uploading many nested files individually is painful on mobile, tell me — I can zip the project and you can use a phone file manager / GitHub's drag-and-drop of a folder instead, or I can walk you through the GitHub mobile app's "add folder" flow.)*

### Step 3 — Let it build
1. As soon as you push to `main`, go to the **Actions** tab in your repo.
2. You'll see a "Build APK" workflow running (takes ~2–3 minutes).
3. When it finishes (green check), tap into that run, scroll to **Artifacts**, and download `health-tracker-debug-apk`.

### Step 4 — Install on your phone
1. Unzip the downloaded artifact to get `app-debug.apk`.
2. Open it directly from your phone's file manager/downloads.
3. Android will ask to allow installs from this source the first time — allow it, then install.

### Step 5 — First-time setup in the app
- Grant notification permission when asked.
- Grant "Alarms & reminders" permission when asked (needed for exact-time medication/water alerts).
- Tap **Start Reminders** under the medication card to begin your 12-hour Cirtel 40 cycle.
- Use **+250ml / +500ml / +1L** buttons to log water through the day.
- Use **Log Episode** to start/end a migraine, tagging triggers and what helped.

## Notes
- All data is stored locally on your device (Room/SQLite) — nothing leaves your phone.
- Reminders survive reboots (the app reschedules them on boot).
- This is a debug build (fine for personal use). If you ever want a signed release build for wider distribution, that's a separate step I can help with.
