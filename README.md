# ☀️ SkySync SA

<p align="center">

### **Simple Weather. Smarter Planning. 🇿🇦**

*A South African-focused Android weather application designed around simplicity, accessibility and reliable weather information.*

</p>

---

# Link to YouTube demonstration video:
https://youtu.be/fJMOFESE5r8?si=FWqF464lUsMW8Riy 


# 📱 1. Application Overview

**SkySync SA** is a planned Android weather application developed to provide users with a fast and understandable way of accessing local weather information.

The application follows a **"forecast first"** design philosophy. Rather than presenting users with a large number of complicated weather features immediately, the application places the most important information at the centre of the experience.

Users can quickly view:

* 🌡️ Current temperature
* ☁️ Current weather condition
* 🌡️ Feels-like temperature
* 🌧️ Rain probability
* 💨 Wind information
* 💧 Humidity
* 🕐 Hourly forecasts
* 📅 Seven-day forecasts
* 📍 Saved locations
* 🚨 Weather alerts

The application also introduces additional features such as **Plan My Day**, offline forecast access, synchronisation, multiple languages and an optional radar experience.

---

# 🎯 2. Purpose of the Application

The primary purpose of SkySync SA is to make weather information **quick to understand and easy to use**.

Research into existing weather applications showed that established applications such as AccuWeather, Weather: Forecast & Radar Maps and Weather & Radar provide extensive information and functionality. However, their large feature sets can also create information density for users who simply want to know what the weather will be like.

SkySync therefore focuses on a smaller set of high-value features.

### Main objectives

**1. Provide fast weather information**

Users should be able to open the application and immediately understand the current weather.

**2. Help users plan their day**

The application provides hourly and seven-day forecasts together with the planned **Plan My Day** feature.

**3. Support South African users**

The application is designed around South African locations and supports English, Afrikaans and isiZulu.

**4. Remain useful offline**

Previously downloaded forecasts can remain available when the user has no internet connection.

**5. Provide useful notifications**

Users can choose weather alert categories that are relevant to them.

**6. Maintain a simple interface**

Complex features such as radar are kept separate from the main forecast so they do not interfere with the primary purpose of the application.

---

# 🎨 3. Design Considerations

## 🌤️ Forecast-First Design

The most important design decision is the **forecast-first approach**.

The home screen prioritises the information users are most likely to need:

```text
┌─────────────────────────────────┐
│         Cape Town               │
│                                 │
│             ☁️                  │
│            22°C                 │
│           Cloudy                │
│                                 │
│ Feels like 21°C                 │
│ Rain 45%     Wind 12 km/h       │
│                                 │
│ ☔ Plan My Day                   │
│ Umbrella recommended after 14:00│
└─────────────────────────────────┘
```

This approach was influenced by the research into existing weather applications, particularly the useful current-weather and short-term forecast information provided by AccuWeather.

---

# 🧭 4. User Interface & Navigation

The application is designed around a straightforward navigation structure.

```text
              ┌──────────────┐
              │    WELCOME   │
              │ Login / Sign │
              │     Up       │
              └──────┬───────┘
                     │
                     ▼
              ┌──────────────┐
              │     HOME     │
              └──────┬───────┘
                     │
       ┌─────────────┼─────────────┐
       ▼             ▼             ▼
  LOCATIONS        RADAR        SETTINGS
```

The planned bottom navigation provides access to:

* 🏠 Home
* 📍 Locations
* 🗺️ Radar
* ⚙️ Settings

Account functionality remains within Settings.

Loading, offline and permission states are designed to use plain-language messages so that users can understand what is happening.

---

# 🎨 5. Visual Design

SkySync SA uses a distinctive visual identity rather than copying the appearance of existing weather applications.

The initial application icon is designed as:

> **A black rounded square containing a yellow sun, orange cloud and three rain strokes.**

The colour combination was selected to create a recognisable weather identity while being visually different from many conventional weather applications.

### Suggested visual identity

| Element      | Design                 |
| ------------ | ---------------------- |
| ☀️ Sun       | Yellow                 |
| ☁️ Cloud     | Orange                 |
| 🌧️ Rain     | Weather-blue/turquoise |
| ⬛ Background | Black                  |
| 🎨 Interface | Clean and modern       |

---

# 📸 6. Application Screens

The planned application includes the following core screens:

### 👋 Welcome / Authentication

Users can:

* Sign in
* Create an account
* Use Google Single Sign-On

### 🏠 Home

Displays the most important current weather information.

### 📊 Forecast Details

Provides:

* Hourly forecast
* Seven-day forecast

### 📍 Locations

Allows users to search for and manage saved locations.

### 🗺️ Radar

Provides an optional visual view of weather conditions.

### ⚙️ Settings

Allows users to customise:

* Language
* Temperature units
* Wind units
* Theme
* Default location
* Data saver
* Alert categories

The system design document defines these screens and their navigation as part of the minimum application interface.

> **📌 Screenshots can be added here once the application prototype has been developed.**
>
> Example:
>
> `![SkySync Home Screen](images/home-screen.png)`

---

# 🌍 7. Accessibility & Localisation

SkySync is designed to accommodate different users and preferences.

### Languages

The planned launch languages are:

* 🇬🇧 English
* 🇿🇦 Afrikaans
* 🇿🇦 isiZulu

All visible text is stored using Android string resources so that translations can be managed separately from the application's code.

### Accessibility

The interface considers:

* Readable labels
* Icons accompanied by text alternatives
* Dark mode
* Clear error messages
* Simple language
* Configurable units

---

# 📡 8. Offline Functionality

A major design consideration is maintaining useful functionality when the user has limited or no connectivity.

**Room** stores the most recent forecast for saved locations.

When offline:

```text
Internet unavailable
        ↓
Load cached forecast
        ↓
Display last updated time
        ↓
User continues using app
```

When connectivity returns:

```text
Internet restored
        ↓
WorkManager detects connection
        ↓
Queued changes synchronised
        ↓
Forecast refreshed
```

The system uses the latest server timestamp when resolving synchronisation conflicts.

---

# 🔔 9. Notifications

SkySync uses **Firebase Cloud Messaging (FCM)** for weather notifications.

Users can choose between:

* 🌧️ Rain starting soon
* ⚠️ Severe weather
* 🌅 Daily morning forecast

Notification channels are used to separate important warnings from routine information.

Severe weather notifications are designed to receive higher priority, while routine forecasts are silent by default.

---

# 🔐 10. Authentication & Security

SkySync uses **Firebase Authentication** to manage user accounts.

Users can register using:

* Email
* Password
* Display name

Existing users can sign in or use Google Single Sign-On.

The application does not store user passwords. Firebase Authentication manages credentials and authentication, while communication uses TLS.

---

# 🏗️ 11. System Design

SkySync uses a layered architecture to separate the user interface, application logic, local storage and external services.

```text
                 ┌───────────────────┐
                 │   Android App     │
                 └─────────┬─────────┘
                           │
                           ▼
                 ┌───────────────────┐
                 │        UI         │
                 └─────────┬─────────┘
                           │
                           ▼
                 ┌───────────────────┐
                 │    ViewModels     │
                 └─────────┬─────────┘
                           │
                           ▼
                 ┌───────────────────┐
                 │   Repositories    │
                 └───────┬─────┬─────┘
                         │     │
                ┌────────┘     └────────┐
                ▼                       ▼
        ┌───────────────┐       ┌───────────────┐
        │  Room Cache   │       │  SkySync API  │
        │    Offline    │       │ Node/Express  │
        └───────────────┘       └───────┬───────┘
                                        │
                                        ▼
                                ┌───────────────┐
                                │   Open-Meteo  │
                                └───────────────┘

                    ┌───────────────────────┐
                    │       Firebase        │
                    ├───────────────────────┤
                    │ Authentication        │
                    │ Firestore             │
                    │ Cloud Messaging       │
                    │ Cloud Functions       │
                    └───────────────────────┘
```

The Android UI communicates with ViewModels, which use repositories to access either the network API or Room cache. This prevents network-specific classes from being exposed directly to the user interface.

---

# 🌐 12. API Design

The application uses a custom **SkySync REST API** built using Node.js and Express.

The API sits between the Android application and Open-Meteo.

```text
Android Application
        │
        ▼
   SkySync API
        │
        ▼
   Open-Meteo API
        │
        ▼
 Weather Information
```

The main planned endpoints are:

| Method | Endpoint               | Purpose                       |
| ------ | ---------------------- | ----------------------------- |
| GET    | `/v1/forecast`         | Retrieve forecast information |
| GET    | `/v1/locations/search` | Search for locations          |
| PUT    | `/v1/user/preferences` | Store user preferences        |

The custom API converts Open-Meteo information into a smaller application-specific response and provides a stable interface for the Android application. It is planned to be hosted on Render as a Docker web service.

---

# 🗄️ 13. Data Storage

SkySync uses both cloud and local storage.

### Firebase / Firestore

Used for:

* User profiles
* Preferences
* Saved locations
* Alert preferences

### Room

Used for:

* Cached forecasts
* Offline information
* Pending synchronisation changes

The application therefore combines cloud storage with local storage to support both synchronisation and offline functionality.

---

# 🔬 14. Research Behind the Design

Before designing SkySync, three existing weather applications were investigated:

### ☀️ AccuWeather

Key areas investigated included:

* Local forecasts
* RealFeel information
* Hourly forecasts
* Weather alerts
* Radar

### 🌧️ Weather: Forecast & Radar Maps

Key areas investigated included:

* Weather warnings
* Notifications
* Radar
* Weather planning
* Widgets

### 🗺️ Weather & Radar Forecast

Key areas investigated included:

* Interactive maps
* Rain and storm information
* Weather layers
* Forecast visualisation

The research identified useful features from each application while also identifying the potential issue of feature-heavy interfaces.
The resulting design decisions were:

| Research Finding                            | SkySync Design Decision         |
| ------------------------------------------- | ------------------------------- |
| Detailed current weather is useful          | Prominent current-weather card  |
| Alerts are useful for planning              | Configurable notifications      |
| Radar provides useful visual context        | Optional radar screen           |
| Large feature sets can overwhelm users      | Simple forecast-first interface |
| Offline access improves usability           | Room forecast caching           |
| South African users require local relevance | SA locations + local languages  |

---

# 🐙 15. GitHub Usage

GitHub is used as the central **source-control and collaboration platform** for the SkySync project.

The project source code, documentation and development history are maintained within the GitHub repository.

### GitHub is used for:

* 📁 Source-code storage
* 🔄 Version control
* 📝 Commit history
* 🌿 Branch management
* 🔀 Merging changes
* 🐛 Issue tracking
* 📋 Project documentation
* ⚙️ Automated workflows

Using GitHub provides a history of changes throughout development and makes it possible to return to previous versions of the project when necessary.

---

# 🌿 16. Git Workflow

Development can be organised using separate branches for different features or tasks.

Example:

```text
main
 │
 ├── feature/authentication
 │
 ├── feature/forecast
 │
 ├── feature/locations
 │
 ├── feature/offline-mode
 │
 └── feature/notifications
```

A typical workflow is:

```text
Create Branch
      ↓
Develop Feature
      ↓
Test Changes
      ↓
Commit
      ↓
Push to GitHub
      ↓
GitHub Actions
      ↓
Build / Test
      ↓
Merge into Main
```

This approach keeps unfinished development separate from the main project branch.

---

# ⚙️ 17. GitHub Actions

**GitHub Actions** is used to automate development tasks whenever changes are pushed to the repository.

The system design specifies GitHub Actions as part of the project's development and delivery process, together with Kotlin, Android Studio and GitHub source control.

A planned workflow can perform automated checks such as:

```text
Developer pushes code
        ↓
GitHub Actions starts
        ↓
Checkout repository
        ↓
Set up Java / Android environment
        ↓
Build project
        ↓
Run unit tests
        ↓
Check result
        ↓
Success ✅ / Failure ❌
```

---

# 🧪 18. Testing

Testing is an important part of the planned development process.

Unit tests will cover areas including:

* Input validation
* Forecast mapping
* Queued synchronisation
* ViewModel states

The final project is also expected to include a signed release build, application icon, privacy notice and Play Store listing material.

GitHub Actions can assist with this process by automatically running build and test commands when changes are pushed.

---

# 📅 19. Development Plan

The project is planned over six weeks.

| Week            | Development Activity                      |
| --------------- | ----------------------------------------- |
| **Week 1**      | Research and scope                        |
| **Week 2**      | Wireframes and API contract               |
| **Week 3**      | Project, Firebase and GitHub setup        |
| **Week 4**      | Authentication and settings               |
| **Week 5**      | Forecast, locations and API               |
| **Week 6**      | Offline sync and languages                |
| **Final Stage** | Notifications, testing, fixes and release |

The plan deliberately includes time for defect correction before final evaluation.

---

# 🛠️ 20. Technology Stack

| Technology                   | Purpose                    |
| ---------------------------- | -------------------------- |
| **Kotlin**                   | Android application        |
| **Android Studio**           | Development environment    |
| **Firebase Authentication**  | User authentication        |
| **Cloud Firestore**          | Cloud data storage         |
| **Firebase Cloud Messaging** | Notifications              |
| **Firebase Cloud Functions** | Alert processing           |
| **Room**                     | Local database             |
| **WorkManager**              | Background synchronisation |
| **Node.js**                  | REST API                   |
| **Express**                  | API framework              |
| **Open-Meteo**               | Weather data               |
| **Docker**                   | API containerisation       |
| **Render**                   | API hosting                |
| **GitHub**                   | Source control             |
| **GitHub Actions**           | Automation and CI          |

---

# 🚀 21. Project Vision

SkySync SA aims to create a weather application that provides the information users actually need without making the experience unnecessarily complicated.

The combination of:

**☀️ Simple Interface**

**📍 Local Forecasts**

**📡 Offline Support**

**🔔 Useful Notifications**

**🌍 South African Language Support**

**🔐 Secure Authentication**

**☁️ Cloud Synchronisation**

**⚙️ Automated Development**

creates the foundation for a practical and maintainable Android application.

The final design is intended to remain focused on its core purpose: **helping users understand the weather and plan their day.**

---

### ☀️ SkySync SA

> **Simple weather. Smarter planning. 🇿🇦**

---

## 📚 References

* [Firebase Documentation](https://firebase.google.com/docs)
* [Android Developers](https://developer.android.com)
* [Open-Meteo Documentation](https://open-meteo.com/en/docs)
* [Render Documentation](https://render.com/docs)
* [AccuWeather — Google Play](https://play.google.com/store/apps/details?id=com.accuweather.android)
* [The Weather Channel — Google Play](https://play.google.com/store/apps/details?id=com.weather.Weather)
* [Weather & Radar — Google Play](https://play.google.com/store/apps/details?id=de.wetteronline.wetterapp)
