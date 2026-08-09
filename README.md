# MyFit — The Sovereign Fitness Ecosystem

A data-driven, privacy-first, 100% on-device, dual-application training system for elite lifters. No cloud accounts, no cellular requirement, no gamification fluff — just precise tracking of training, nutrition, and recovery.

## Why

Mainstream fitness apps force cloud connectivity, leak health telemetry, break down in gym-basement dead zones, and paper over serious training with streaks and confetti. MyFit rejects all of that.

## Core Principles

- **Dual-Device Autonomy (Peer-to-Peer)** — The phone and watch apps are fully independent peers. Run a workout on either device alone, or both together. They sync over Bluetooth Low Energy and resolve conflicts silently using event timestamps.
- **Absolute Data Sovereignty** — All tracking logic, biometric processing, and AI inference run locally. No external servers, no mandatory accounts, no internet dependency.
- **No-Bullshit Utility** — Strips lifestyle fluff. Measures bar velocity, tracks systemic CNS fatigue, and logs nutrition with minimal friction.

## Project Structure

This is a multi-module Android project (Kotlin + Jetpack Compose) targeting phone and Wear OS.

```
MyFitApp/
├── app/    # Phone application (minSdk 31, targetSdk 35)
│   └── src/main/java/com/myfit/
│       ├── core/           # Shared infrastructure
│       ├── ble/            # Bluetooth Low Energy sync with the watch
│       ├── navigation/     # App-level navigation graph
│       └── feature/
│           ├── home/
│           ├── planner/
│           ├── training/
│           ├── nutrition/
│           ├── onboarding/
│           └── analytics/
└── wear/   # Wear OS application (minSdk 30, targetSdk 35)
    └── src/main/java/com/myfit/wear/
        ├── di/             # Dependency injection
        ├── ble/            # Bluetooth Low Energy sync with the phone
        ├── navigation/     # Wear navigation graph
        ├── data/           # Room entities and DAOs
        └── feature/
            └── workout/
```

### Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3 on phone, Wear Compose on watch)
- **DI**: Hilt
- **Persistence**: Room + DataStore
- **Async**: Kotlin Coroutines
- **Connectivity**: Bluetooth Low Energy (BLE) for phone ↔ watch sync

## Getting Started

### Prerequisites

- Android Studio (latest stable)
- JDK 17

### Build & Run

```bash
./gradlew assembleDebug        # build the phone app
./gradlew :wear:assembleDebug  # build the watch app
```

Open the project in Android Studio and run the `app` or `wear` configuration on a device/emulator.

## License

Private project — all rights reserved.
