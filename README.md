# MDRRMO Balilihan — Hazard Reporting Mobile App

A mobile application for the **Municipal Disaster Risk Reduction and Management Office (MDRRMO)** of Balilihan, Bohol. Built to enable authorized reporters to submit real-time hazard incidents with photos, GPS coordinates, and severity levels.

---

## 📱 Features

- **Hazard Report Submission** — Capture photo, auto-attach GPS coordinates, select hazard type and risk level
- **Nearby Hazard Map** — Google Maps with color-coded hazard pins by severity
- **Hazard Alerts Feed** — All active hazards in Balilihan with read/unread state
- **Report History** — View all submitted reports with filter by status
- **Offline Support** — Reports saved locally when no internet, auto-synced when reconnected
- **Push Notifications** — Firebase FCM alerts for nearby hazards and report status updates
- **Profile Management** — Edit profile, view report statistics, sign out

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Mobile | Android (Java) |
| Backend | Spring Boot (Java) |
| Local Database | Room (SQLite) |
| Remote Database | PostgreSQL |
| Networking | Retrofit2 + OkHttp |
| Maps | Google Maps SDK |
| Camera | CameraX |
| Push Notifications | Firebase Cloud Messaging (FCM) |
| Background Sync | WorkManager |
| Image Loading | Glide |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17
- Google Maps API key
- Firebase project with FCM enabled

### Setup

1. **Clone the repository**

```bash
git clone https://github.com/earlyabarquez/MDRRMO-App.git
cd MDRRMO-App
```

2. **Add `local.properties`** in the root directory:

```properties
sdk.dir=YOUR_ANDROID_SDK_PATH
GOOGLE_MAPS_API_KEY=your_google_maps_api_key_here
```

3. **Add `google-services.json`** from Firebase Console into the `app/` directory

4. **Configure Spring Boot backend URL** in `network/ApiClient.java`:

```java
private static final String BASE_URL = "http://your_server_ip:8080/";
```

5. **Sync Gradle** and run the app

---

## 📁 Project Structure
app/src/main/java/com/balilihan/mdrrmo/
├── activities/      — Login, Register, Main, Splash
├── fragments/       — Map, Reports, Alerts, Profile, ReportDetail
├── report/          — Camera, Form, Preview, ViewModel
├── viewmodels/      — Business logic separated from UI
├── models/          — Data objects matching Spring Boot API
├── network/         — Retrofit API client and endpoints
├── database/        — Room local database, DAOs
├── services/        — FCM, Location, Sync (WorkManager)
├── adapters/        — RecyclerView adapters
└── utils/           — Session, Network, Location, Image, Date helpers

---

## 🗂️ Database Schema

The app connects to a PostgreSQL database via Spring Boot with the following core tables:

- `users` — Reporter accounts
- `reports` — Hazard report submissions
- `hazard` — Hazard type definitions
- `barangay` — Barangay reference data
- `status` — Report status definitions
- `roles` — User role definitions

---

## 🔑 Environment Variables

| Variable | Location | Description |
|---|---|---|
| `GOOGLE_MAPS_API_KEY` | `local.properties` | Google Maps SDK key |
| Firebase config | `app/google-services.json` | FCM push notifications |
| `BASE_URL` | `ApiClient.java` | Spring Boot server URL |

---

## 📸 Screenshots

> Coming soon — will be added after UI testing is complete

---

## 👥 Team

- **Developer** — Early Abarquez
- **Institution** — Bisayas State University (BISU)
- **Client** — MDRRMO Balilihan, Bohol

---

## 📌 Development Status

| Step | Feature | Status |
|---|---|---|
| 1 | Project setup + Firebase | ✅ Done |
| 2 | Login + Register | ✅ Done |
| 3 | Main navigation + FAB | ✅ Done |
| 4 | Report Camera + Form + Room DB | ✅ Done |
| 5 | Map Fragment + hazard pins | ✅ Done |
| 6 | Alerts Fragment | ✅ Done |
| 7 | Reports History + Sync | ✅ Done |
| 8 | Profile Fragment | ✅ Done |
| 9 | Spring Boot API integration | 🔄 In progress |

---

## ⚠️ Notes

- `local.properties` and `google-services.json` are excluded from version control
- Mock data is used for UI testing until Spring Boot backend is connected in Step 9
- Remove mock login credentials before production deployment

---

## 📄 License

This project is developed as a thesis requirement for **Bisayas State University (BISU)** and is intended for use by **MDRRMO Balilihan, Bohol**.
