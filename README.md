<h1 align="center">👟 StepWise — Pedometer Android App 📈</h1>

<p align="center">
  <em>A polished <strong>Kotlin</strong> Android pedometer that tracks your daily steps in real-time with the device’s built-in step sensor,
  syncs everything to <strong>Firebase</strong>, celebrates your goal with confetti 🎉,
  and shows a 7-day history. ✨</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" alt="Firebase">
  <img src="https://img.shields.io/badge/Material%20Design-757575?style=for-the-badge&logo=materialdesign&logoColor=white" alt="Material">
  <img src="https://img.shields.io/badge/Glide-4285F4?style=for-the-badge&logo=android&logoColor=white" alt="Glide">
  <img src="https://img.shields.io/badge/Gradle-KTS-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="Gradle">
  <img src="https://img.shields.io/badge/Android%20Studio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white" alt="Android Studio">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/minSdk-24-orange?style=flat-square" alt="minSdk">
  <img src="https://img.shields.io/badge/targetSdk-34-blue?style=flat-square" alt="targetSdk">
  <img src="https://img.shields.io/badge/compileSdk-34-blueviolet?style=flat-square" alt="compileSdk">
  <img src="https://img.shields.io/badge/version-1.0-success?style=flat-square" alt="version">
  <img src="https://img.shields.io/badge/status-active-brightgreen?style=flat-square" alt="status">
  <img src="https://img.shields.io/badge/auth-Firebase-FFCA28?style=flat-square" alt="firebase auth">
  <img src="https://img.shields.io/badge/PRs-welcome-ff69b4?style=flat-square" alt="PRs">
  <img src="https://img.shields.io/badge/license-MIT-blue?style=flat-square" alt="license">
  <img src="https://img.shields.io/badge/made%20with-%E2%9D%A4-red?style=flat-square" alt="made with love">
</p>

---

## 🌟 Overview

**StepWise** is a complete fitness-tracker Android app written in **Kotlin**. It uses the **`TYPE_STEP_COUNTER`** hardware sensor to count every step you take, runs a **foreground service** so counting keeps going even when the app is in the background, and persists daily totals to **Firebase Realtime Database** under your account.

It’s designed to feel like a real product — not a demo:

- 🔐 Full **Firebase Authentication** (sign-up, login, forgot-password, profile picture upload to Firebase Storage).
- 🎯 **User-set daily goal** (1,000 → 30,000 steps) saved in `SharedPreferences`.
- 🟢 **Live circular progress** of today’s steps vs your goal, with a confetti **celebration** when you hit it 🎉.
- 🛰️ **Persistent foreground notification** so the OS doesn’t kill the counter.
- 🌙 **Auto-reset at midnight** + a daily Firebase sync via `AlarmManager` / `WorkManager`.
- 📅 **7-day history** screen powered by `RecyclerView` reading back from Firebase.
- 🛡️ Runtime permission handling for `ACTIVITY_RECOGNITION`, notifications, media, camera, etc.
- 🌗 Built-in **dark theme** (`values-night/themes.xml`).

---

## 📑 Table of Contents

- [✨ Features](#-features)
- [🧭 App Flow](#-app-flow)
- [🧠 How Step Counting Works](#-how-step-counting-works)
- [📁 Project Structure](#-project-structure)
- [🛠️ Tech Stack](#%EF%B8%8F-tech-stack)
- [🔐 Permissions Used](#-permissions-used)
- [📋 Requirements](#-requirements)
- [🔥 Firebase Setup](#-firebase-setup)
- [⚙️ Build & Run](#%EF%B8%8F-build--run)
- [📲 Using the App](#-using-the-app)
- [🧰 Troubleshooting](#-troubleshooting)
- [🌱 Future Improvements](#-future-improvements)
- [📜 License](#-license)
- [👤 Author](#-author)

---

## ✨ Features

- 👟 **Real-time step counting** with the hardware step sensor (`Sensor.TYPE_STEP_COUNTER`).
- 🛰️ **Foreground service** (`MyForegroundService`) keeps tracking alive in the background, with a sticky "Pedometer is running" notification.
- 🎯 **Adjustable daily goal** via a smooth `SeekBar` (1k–30k steps), stored in `SharedPreferences`.
- 🎉 **Konfetti** confetti animation when the daily goal is reached.
- 🟢 **Circular progress bar** (`com.mikhaellopez:circularprogressbar`) showing today’s % completion.
- 📏 **Auto-calculated distance** (in km) and **calories** burned from steps.
- 🔐 **Firebase Auth** — Email/Password sign up, login, password reset.
- 🖼️ **Profile picture upload** to **Firebase Storage** with **Glide** image loading + `CircleImageView`.
- ☁️ **Firebase Realtime Database** sync of every day’s totals (`/steps/{uid}/{date}`).
- 📅 **7-day step history** screen with day-name labels (Mon, Tue…) backed by a custom `RecyclerView` adapter.
- 🌙 **Midnight auto-reset** (via `MidnightReceiver`, `AlarmReceiver`, and an optional `WorkManager` `StepResetWorker`).
- 🌐 **Network-aware** UX — `NetworkUtils` checks Wi-Fi / cellular before Firebase calls.
- 🚪 Custom **exit-confirmation** dialog (`ShowDialogBox`) on back-press.
- 🧭 **Material Navigation Drawer** with profile header, history, settings, and logout.
- 🌓 **Light & dark theme** support out of the box.

---

## 🧭 App Flow

```
   ┌────────────────────────┐
   │ 🚀 Splash_Screen       │  → asks for permissions
   └──────────┬─────────────┘
              ▼
     not logged in?           already logged in?
       │                         │
       ▼                         ▼
   ┌────────┐               ┌────────────────┐
   │ Login  │ ◀── forgot ──▶│ Main_Screen 🟢 │
   └───┬────┘   VerifyCode  │ (live counter) │
       │                    └───┬────────────┘
       ▼                        │
   ┌────────┐                   ├─▶ 📅 History  (7-day Firebase fetch)
   │ SignUP │                   ├─▶ ⚙️ Menu     (set goal, view distance/cal)
   └────────┘                   └─▶ 🚪 Logout   (signOut + back to Login)
                                    │
                                    └─▶ 🛰️ MyForegroundService
                                          ├─ TYPE_STEP_COUNTER sensor
                                          ├─ AlarmReceiver (periodic Firebase push)
                                          └─ MidnightReceiver / StepResetWorker
                                              (reset + sync at 00:00)
```

---

## 🧠 How Step Counting Works

1. `Main_Screen` registers a `SensorEventListener` for `Sensor.TYPE_STEP_COUNTER`.
2. The **cumulative** sensor value is offset by the value captured at midnight, so the displayed count is "today’s steps".
3. A `CircularProgressBar` is updated against `goal` (default `10,000`, configurable in `Menu`).
4. The **`MyForegroundService`** runs the same logic in the background and posts a sticky notification, so the OS won’t kill the process.
5. **`AlarmManager`** schedules `AlarmReceiver` to push the current step count to Firebase periodically (`SEND_STEPS_TO_FIREBASE` action).
6. **`MidnightReceiver`** listens for `ACTION_DATE_CHANGED` / `ACTION_TIME_CHANGED` and resets `SharedPreferences["key1"]` (today’s base value) to `0`, so a new day starts fresh.
7. **`StepResetWorker`** (WorkManager) is the modern, alarm-friendly fallback for the same reset.
8. Each day’s final count is written under `steps/{uid}/{date}` in Firebase Realtime Database; **`History`** reads back the last 7 entries.

Distance and calories are derived from steps:

```
distance_km   = steps × 0.000762   // ~76.2 cm stride
calories_kcal = steps × 0.04       // common rough estimate
```

---

## 📁 Project Structure

```
StepWise-Pedometer-Android/
├── app/
│   ├── build.gradle.kts                  # 🛠️ Module Gradle (Kotlin DSL) + Firebase plugin
│   ├── google-services.json              # 🔥 Firebase config
│   ├── proguard-rules.pro
│   ├── AlarmReceiver.kt                  # ⏰ (referenced) Triggers Firebase push from alarm
│   ├── MidnightReceiver.kt               # 🌙 Resets today’s steps at date change
│   ├── StepResetWorker.kt                # 🔁 WorkManager job for the same reset
│   ├── StepsResetBroadcastReceiver.kt    # 📣 Local broadcast for reset events
│   ├── CustomCircularProgressIndicator.kt# 🟢 Custom progress widget
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml       # 📜 Activities, services, permissions
│       │   ├── java/com/muhammadahmad/pedometer/
│       │   │   ├── Splash_Screen.kt              # 🚀 Permissions + routing
│       │   │   ├── Login.kt                      # 🔐 Firebase email/password sign-in
│       │   │   ├── SignUP.kt                     # 📝 Sign-up + profile pic to Storage
│       │   │   ├── VerifyCode.kt                 # 📧 Forgot-password reset email
│       │   │   ├── Main_Screen.kt                # 🟢 Step counter + progress + drawer
│       │   │   ├── Menu.kt                       # 🎯 Goal slider + distance/calories
│       │   │   ├── History.kt                    # 📅 Last 7 days of steps
│       │   │   ├── StepItem.kt                   # 📦 Day / steps / cal / distance model
│       │   │   ├── StepItemAdapter.kt            # 🧱 RecyclerView adapter
│       │   │   ├── MyForegroundService.kt        # 🛰️ Sticky service + sensor + alarms
│       │   │   ├── StepCountManager.kt           # 🛰️ Alternate foreground sensor service
│       │   │   ├── AlarmReceiver.kt              # ⏰ In-package alarm receiver
│       │   │   ├── StepsResetBroadcastReceiver.kt# 📣 In-package reset receiver
│       │   │   ├── ShowDialogBox.kt              # 🚪 Exit confirmation dialog
│       │   │   ├── CustomGifProgressBar.kt       # ⏳ Loading screen with GIF (Glide)
│       │   │   ├── NetworkUtils.kt               # 🌐 Wi-Fi / cellular check
│       │   │   └── UserData.kt                   # 👤 User profile data class
│       │   └── res/                              # 🎨 Layouts, drawables, mipmaps, themes
│       │       ├── layout/                       #     activity_*.xml + dialogs + nav_header
│       │       ├── drawable/                     #     icons, gradients, seekbar, GIF loader
│       │       ├── menu/drawer_menu.xml          #     Side-drawer items
│       │       ├── values/                       #     colors, strings, light theme
│       │       └── values-night/themes.xml       #     🌙 Dark theme
│       └── androidTest/                          # 🧪 Instrumented test scaffold
├── build.gradle.kts                      # 📦 Top-level: AGP 8.2.2, Kotlin 1.9.22, GMS 4.4.2
├── settings.gradle.kts                   # 🏷️ rootProject.name = "Pedometer"
├── gradle.properties
├── gradlew / gradlew.bat                 # 🐘 Gradle wrapper
└── README.md
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| 💚 **Language** | Kotlin |
| 🤖 **Platform** | Android (`minSdk 24`, `targetSdk 34`, `compileSdk 34`) |
| 🧰 **Build** | Gradle Kotlin DSL + AGP **8.2.2**, Kotlin **1.9.22**, Google Services **4.4.2** |
| 🎨 **UI** | AppCompat **1.6.1**, Material **1.12.0**, ConstraintLayout **2.1.4** |
| 🟢 **Progress** | `com.mikhaellopez:circularprogressbar` **3.1.0** |
| 🎉 **Confetti** | `nl.dionsegijn:konfetti-xml` **2.0.4** |
| 🖼️ **Image loading** | Glide **4.12.0** + `de.hdodenhof:circleimageview` **3.1.0** |
| 🔐 **Auth** | Firebase Auth (KTX) **23.0.0** |
| ☁️ **Database** | Firebase Realtime Database (KTX) **21.0.0** |
| 📦 **Storage** | Firebase Storage (KTX) **21.0.0** |
| ⏱️ **Background work** | `androidx.work:work-runtime-ktx` **2.7.1**, `AlarmManager`, foreground service |
| 🧪 **Tests** | JUnit 4.13.2 + AndroidX Test 1.1.5 + Espresso 3.5.1 |

---

## 🔐 Permissions Used

| Permission | Why |
|---|---|
| `ACTIVITY_RECOGNITION` | Required to read `TYPE_STEP_COUNTER` on API 29+. |
| `POST_NOTIFICATIONS` | Sticky pedometer notification (Android 13+). |
| `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_DATA_SYNC` | Run `MyForegroundService` while syncing steps to Firebase. |
| `READ_MEDIA_IMAGES` / `READ_MEDIA_VIDEO` | Pick a profile picture (Android 13+). |
| `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` | Profile picture access on pre-Android-13. |
| `CAMERA` | Optionally take a profile picture. |

---

## 📋 Requirements

- 🟢 **Android Studio** Iguana / Hedgehog or newer.
- ☕ **JDK 17** (bundled with recent Android Studio).
- 🤖 **Physical Android device** with a step-counter sensor (most phones have one) running **API 24+**. Emulators usually don’t have a real step sensor.
- 🔥 A **Firebase** project (Auth + Realtime DB + Storage enabled).

---

## 🔥 Firebase Setup

1. Create a project at <https://console.firebase.google.com>.
2. Add an **Android app** with package name **`com.muhammadahmad.pedometer`**.
3. Download the generated **`google-services.json`** and place it at **`app/google-services.json`** (replacing the one in the repo).
4. In the Firebase console:
   - Enable **Authentication → Email/Password**.
   - Enable **Realtime Database** (start in test mode, then lock down with rules).
   - Enable **Storage** (for profile pictures).
5. Recommended Realtime Database rules to start:

   ```json
   {
     "rules": {
       "user":  { "$uid": { ".read": "$uid === auth.uid", ".write": "$uid === auth.uid" } },
       "steps": { "$uid": { ".read": "$uid === auth.uid", ".write": "$uid === auth.uid" } }
     }
   }
   ```

---

## ⚙️ Build & Run

### 1️⃣ Clone

```bash
git clone https://github.com/ahmadimran-15/StepWise-Pedometer-Android.git
cd StepWise-Pedometer-Android
```

### 2️⃣ Open in Android Studio

`File → Open…` → select the folder. Let Gradle sync (it pulls AGP 8.2.2 + Kotlin 1.9.22 the first time).

### 3️⃣ Plug in a real device

Enable **Developer options → USB debugging**, then click **▶ Run**.

### 🧪 Or build from the command line

```bash
# Linux / macOS
./gradlew assembleDebug
# Windows
gradlew.bat assembleDebug
```

The debug APK will be at:

```
app/build/outputs/apk/debug/app-debug.apk
```

For a release build:

```bash
./gradlew assembleRelease
```

---

## 📲 Using the App

1. 🚀 **Launch** — accept all runtime permissions on the splash screen (especially **Activity Recognition** and **Notifications**).
2. 📝 **Sign up** with email + password, optionally pick a profile picture.
3. 🔐 **Log in** — or tap **Forgot Password** to email yourself a reset link.
4. 🟢 On the **Main Screen** you’ll see the live step count, the circular progress bar, and your distance + calories.
5. 🍔 Open the **drawer** to:
   - **History** → see your last 7 days.
   - **Menu** → drag the slider to set a new daily goal (1k–30k).
   - **Logout** → signs you out and returns to Login.
6. 🎉 When you cross your goal, **konfetti** rains across the screen — congrats!
7. 🌙 At midnight, the counter automatically resets and yesterday’s total stays in your **History**.

---

## 🧰 Troubleshooting

| 🚧 Problem | 💡 Fix |
|---|---|
| Steps stay at `0` | Most emulators have no step sensor — test on a real phone. |
| Step counter freezes when app is closed | Make sure you allowed the **persistent notification** and disabled battery optimization for the app (`Settings → Apps → StepWise → Battery → Unrestricted`). |
| Permission dialogs keep returning | The splash re-checks on `onResume`. Grant **Activity Recognition** + **Notifications** in Settings. |
| Firebase: `PERMISSION_DENIED` | Update your Realtime DB rules (see [Firebase Setup](#-firebase-setup)). |
| `google-services.json` error | The file in the repo is tied to its original Firebase project — replace it with your own. |
| Login keeps spinning | The `Login` screen has a 12-second timeout — usually means the device is offline (`NetworkUtils` will tell you). |
| History is empty | Make sure the foreground service has run for at least one full day so a daily total has been written to Firebase. |
| Steps reset mid-day | Some OEM Androids kill `AlarmManager` aggressively — keep battery optimization off for StepWise. |

---

## 🌱 Future Improvements

- 🧱 Move data layer to **Room** as an offline cache + Firebase as remote.
- 📈 Replace `RecyclerView` history with **MPAndroidChart** weekly/monthly graphs.
- 🏆 **Achievements & streaks** (7-day streak, 100k-step badge, etc.).
- 📲 **Home-screen widget** with today’s progress ring.
- ⌚ **Wear OS companion** to count from a smartwatch.
- 🌐 **Localization** (Urdu + English).
- 🧪 Real **unit + UI tests** for the step-math and Firebase code.
- 🎨 Migrate the Main Screen to **Jetpack Compose**.
- 🔒 Move Auth to **Credential Manager** / **Sign in with Google**.

---

## 📜 License

Released under the **MIT License** — free to use, modify, and share. ❤️

---

## 👤 Author

<p>
  <strong>Ahmad Imran</strong><br>
  🐙 GitHub: <a href="https://github.com/ahmadimran-15">@ahmadimran-15</a>
</p>

<p align="center">
  ⭐ <em>If StepWise helped you walk a little more today, please drop a star on the repo!</em> ⭐
</p>
