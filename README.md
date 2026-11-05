# 🌤️ SkySync SA

<h4><strong>▶️ Link to YouTube Demonstration Video</strong></h4>

https://youtu.be/kAMFe8HJ2o4?si=8lVkvETiK-oUikRo 

<h4><strong>📱 1. Application Overview</strong></h4>

SkySync SA is an Android weather application that provides users with a fast and understandable overview of local weather conditions. It follows a forecast-first design philosophy, displaying the most important information upfront while keeping additional features, such as the radar map and detailed forecasts, accessible without cluttering the home screen.

Users can view:

- 🌡️ Current temperature and weather conditions
- 🌡️ Feels-like temperature
- 💧 Humidity and wind speed
- 🌧️ Rain probability in the hourly forecast
- 🕐 Hourly forecast information
- 📅 Seven-day forecast information
- 📍 Saved locations and current-location weather
- 🗺️ Interactive rainfall radar
- 🚨 Configurable weather alerts

The final application also includes Firebase user accounts, Google single sign-on, offline forecast access, offline settings synchronisation, real-time notification support, and English, Afrikaans, and isiZulu language options.

<h4><strong>🎯 2. Purpose of the Application</strong></h4>

The purpose of SkySync SA is to make weather information quick to understand and easy to act upon. Many established weather applications present large amounts of information simultaneously. SkySync SA maintains a focused experience by prioritising the details users need to plan travel, clothing, and outdoor activities.

**Main objectives:**

**1. Provide fast weather information**

The home screen displays the selected location, current temperature, and weather conditions before presenting secondary information.

**2. Help users plan their day**

Hourly and seven-day forecasts provide immediate and longer-term weather information.

**3. Support South African users**

The application supports English, Afrikaans, and isiZulu, uses the Africa/Johannesburg timezone for forecasts, and is designed around local weather planning.

**4. Remain useful offline**

The last successfully retrieved forecast is stored in SQLite and remains accessible without an internet connection.

**5. Provide useful alerts**

Users can manage rain, severe-weather, and daily forecast notifications through Settings.

**6. Protect user information**

Firebase Authentication securely manages credentials, while SkySync SA avoids storing raw passwords in Firestore.

<h4><strong>🎨 3. Design Considerations</strong></h4>

**🌤️ Forecast-First Design**

The main design decision is the forecast-first approach. The home screen prioritises current weather conditions while keeping additional features accessible with minimal navigation.

**🖤 Visual Identity**

SkySync SA uses a distinctive black, orange, and yellow visual system. The dark background provides strong contrast, orange highlights primary actions, and yellow reinforces the weather and sunshine theme. Rounded cards group related information and maintain visual consistency.

| Element | Design Choice |
|---|---|
| ☀️ Accent | Yellow for weather highlights |
| 🟧 Primary actions | Orange buttons and headers |
| ⬛ Background | Dark, high-contrast interface |
| 📦 Content | Rounded cards with clear spacing |
| 🗺️ Radar | Separate screen to keep the home page focused |

**♿ Accessibility and Localisation**

The interface incorporates readable labels, high contrast, large touch targets, and straightforward language.

Supported languages:

- English
- Afrikaans
- isiZulu

<h4><strong>🧭 4. User Interface and Navigation</strong></h4>

**👋 Welcome and Authentication**

Users can register or sign in using an email address and password through Firebase Authentication. The white **Continue with Google** button provides Google single sign-on.

**Login / Sign Up Page**

<!-- Insert Login / Sign Up screenshot here -->

**🏠 Home**

The Home screen retrieves live weather data from Open-Meteo and displays current conditions, a summary of the upcoming forecast, and navigation to Details, Radar Map, and Settings.

**Homepage**

<!-- Insert Homepage screenshot here -->

**📊 Forecast Details**

The Details screen provides hourly and seven-day forecast information. It also explains the application's offline caching and synchronisation behaviour.

**Forecast Details Page**

<!-- Insert Forecast Details screenshot here -->

**🗺️ Radar Map**

The optional RainViewer map provides a visual rainfall-radar experience without overcrowding the main weather screen.

**Radar Map**

<!-- Insert Radar Map screenshot here -->

**⚙️ Settings**

Users can select a language, set a default location, save locations, request current-location weather, and manage rain, severe-weather, and daily forecast alerts.

Settings are saved locally first and synchronised with Firestore when an internet connection is available.

**Settings Page**

<!-- Insert Settings screenshot here -->

<h4><strong>📡 5. Live Weather Data and API Integration</strong></h4>

SkySync SA connects directly to the Open-Meteo REST API for live weather data and location searches.

The application uses Open-Meteo geocoding to convert a city name into coordinates before requesting current, hourly, and daily forecast information.

**External Services**

| Service | Purpose |
|---|---|
| [Open-Meteo](https://open-meteo.com/en/docs) | Live current, hourly, and daily forecast data |
| [RainViewer](https://www.rainviewer.com/) | Interactive rainfall radar map |
| Firebase Authentication | Email/password accounts and Google Sign-In |
| Cloud Firestore | Cloud user settings and notification-token storage |
| Firebase Cloud Messaging | Remote weather notifications |

<h4><strong>📶 6. Offline Mode and Synchronisation</strong></h4>

SkySync SA provides offline functionality through SQLite.

When a forecast is successfully retrieved, the application stores the raw forecast data locally. If a subsequent network request fails, the application loads the most recently saved forecast and identifies it as cached data.

Settings changes are also supported offline. The latest location, language, and alert preferences are stored in a SQLite queue before a cloud update is attempted.

When the device reconnects, the application synchronises the queued settings with Firestore and clears the local queue only after the update succeeds.

**Offline Settings Synchronisation Workflow**

```text
User changes settings while offline
             ↓
SQLite stores the latest pending settings
             ↓
User continues using SkySync SA
             ↓
Network becomes available
             ↓
Queued settings synchronise to Firestore
             ↓
Local queue is cleared after success
```

<h4><strong>🔔 7. Real-Time Notifications</strong></h4>

SkySync SA includes two complementary notification mechanisms:

**Local Weather Alerts**

The current forecast is checked for rain and severe-weather conditions. Alerts follow the user's Settings preferences, and duplicate notifications are limited to one per type per hour.

**Firebase Cloud Messaging**

The application registers a device token after sign-in, stores it in the authenticated user's Firestore profile, and receives remote weather notifications through `SkySyncMessagingService`.

Users can configure:

- 🌧️ Rain starting soon alerts
- ⚠️ Severe weather alerts
- 🌅 Daily morning forecast alerts

The `weather_alerts` Android notification channel uses high importance to help ensure urgent weather notifications remain visible.

<h4><strong>🔐 8. Authentication and Security</strong></h4>

SkySync SA uses Firebase Authentication for account management.

Users can:

- Register with an email address and password.
- Sign in using an existing email/password account.
- Continue with Google through Google single sign-on.
- Sign out through Settings.

Firebase Authentication manages passwords and secure credential handling. SkySync SA does not store passwords in Firestore.

Firestore stores user profile preferences under:

```text
users/{uid}
```

Here, `{uid}` represents the authenticated Firebase user's unique identifier.

<h4><strong>🏗️ 9. System Design</strong></h4>

The application is developed using Kotlin in Android Studio.

The main components include:

| Component | Responsibility |
|---|---|
| `MainActivity` | Manages screen navigation, API calls, settings, authentication, and alert controls |
| `WeatherStore` | Provides SQLite persistence for cached forecasts, saved locations, and pending settings changes |
| `SkySyncMessagingService` | Handles remote Firebase Cloud Messaging notifications |

<h4><strong>🐙 10. GitHub Usage</strong></h4>

GitHub serves as the central source-control and documentation platform for SkySync SA.

The repository contains the Android Studio project, README documentation, release notes, screenshots or visual assets, and the GitHub Actions workflow.

GitHub is used for:

- 📁 Source-code storage
- 🔄 Version control and commit history
- 🌿 Feature branches and pull requests
- 🐛 Issue tracking and defect management
- 📝 Project documentation
- ⚙️ Continuous integration through GitHub Actions

<h4><strong>🚀 11. Release Notes – Final Version 1.0.0</strong></h4>

This final release develops the original weather prototype into a connected Android application.

The following features were added or improved:

| Feature | Final-Version Update |
|---|---|
| 🔐 Firebase accounts | Added secure email/password registration and login through Firebase Authentication |
| 🌐 Google SSO | Added a white Continue with Google button and Firebase Google Sign-In flow |
| 📡 Live REST data | Added Open-Meteo geocoding and forecast requests for current, hourly, and seven-day weather |
| 📶 Offline with sync | Added SQLite forecast caching and a local settings queue that synchronises to Firestore after reconnection |
| 🔔 Real-time alerts | Added high-priority notification channels, forecast alerts, FCM token registration, and an FCM message receiver |
| 🌍 Local languages | Added English, Afrikaans, and isiZulu options |
| 📍 Location support | Added saved locations and current-device location support |
| 🗺️ Optional radar | Added a separate RainViewer radar-map screen |
| 🎨 Final identity | Applied the final black, orange, and yellow interface and removed prototype wording |

<h4><strong>🛠️ 12. Technology Stack</strong></h4>

| Technology | Purpose |
|---|---|
| Kotlin | Android application programming language |
| Android Studio | Development environment |
| Firebase Authentication | Email/password authentication and Google single sign-on |
| Cloud Firestore | User preferences and FCM device-token storage |
| Firebase Cloud Messaging | Remote notification delivery |
| SQLite | Cached forecasts, saved locations, and offline settings queue |
| Open-Meteo | Live weather and geocoding REST API |
| RainViewer | Interactive rainfall radar map |
| GitHub | Source control and documentation |
| GitHub Actions | Automated Android build and test workflow |

<h4><strong>📚 References</strong></h4>

- [Firebase Documentation](https://firebase.google.com/docs)
- [Firebase Authentication for Android](https://firebase.google.com/docs/auth/android/start)
- [Firebase Cloud Messaging for Android](https://firebase.google.com/docs/cloud-messaging/android/get-started)
- [Open-Meteo Documentation](https://open-meteo.com/en/docs)
- [RainViewer](https://www.rainviewer.com/)
- [Android Developers](https://developer.android.com)
- [GitHub Actions Documentation](https://docs.github.com/actions)
