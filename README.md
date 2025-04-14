# 📻 Kotlin Internet Radio App – Lab 5

This is a fully functional **Internet Radio** Android app built with **Jetpack Compose**.  
It was developed for Lab 5 of Mobile App Development, fulfilling all core requirements.

---

## ✅ Features

- 🎧 Stream from **10 Internet Radio Stations**
- 🖼️ Custom station **images**
- 🔊 **Volume Up / Down** controls
- 🎮 **Play / Pause** per station
- 🚀 Runs **MediaPlayer on a HandlerThread** (background-safe)
- 📲 **Plays audio in background** using ForegroundService
- 🔔 **Notification controls** for playback while app is backgrounded

---

## 📷 Screenshots & Diagrams

### App Architecture

```mermaid
flowchart TD
    User(Device) -->|Selects Station| MainActivity
    MainActivity -->|Updates UI| RadioViewModel
    MainActivity -->|Start Service| RadioService
    RadioService -->|Creates| MediaPlayer
    MediaPlayer -->|Streams Audio| Internet
    RadioViewModel -->|Updates| MainActivity
    MainActivity -->|Volume Control| MediaPlayer
    MainActivity -->|Stop/Release| MediaPlayer
    MainActivity -->|Background Playback| HandlerThread
```

---

### User Flow

```mermaid
flowchart TD
    Start([Open App])
    Start --> HomeScreen
    HomeScreen -->|User Clicks Station| PlayRadio
    PlayRadio -->|Start MediaPlayer| StreamAudio
    StreamAudio -->|Audio Playing| ShowControls
    ShowControls -->|User Adjusts Volume| AdjustVolume
    AdjustVolume --> ShowControls
    ShowControls -->|User Selects Another Station| StopCurrent
    StopCurrent --> PlayRadio
    ShowControls -->|User Minimizes App| BackgroundPlay
    BackgroundPlay -->|Continues Playing| StreamAudio
    ShowControls -->|User Closes App| StopAll
    StopAll --> End([End])
```

---

## 🚀 How to Run

1. Clone the repo
2. Open in **Android Studio**
3. Run the app on emulator/device
4. Tap any station to start streaming 🎶

---

## 📁 Submission Files

- ✅ `internet_radio_documentation_fixed.pdf` – official submission document

---

## 🛠 Tech Stack

- Jetpack Compose
- Kotlin
- MediaPlayer
- HandlerThread / Looper
- ForegroundService + Notification

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).