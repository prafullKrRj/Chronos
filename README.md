<div align="center">
  <h1>⏰ Chronos</h1>
  <p><em>A Smart Android Reminder Application</em></p>
  
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Compose">
  <img src="https://img.shields.io/badge/Android-34A853?style=for-the-badge&logo=android&logoColor=white" alt="Android">
</div>

---

## ✨ About

Chronos is an intuitive Android reminder application that helps you stay organized and never miss important tasks. Built with modern Android architecture and featuring a beautiful Material 3 design, it offers seamless reminder management with smart notifications and user-friendly interface.

## 📱 Download

[📲 Download APK](https://drive.google.com/file/d/1vgTGuSRG_o70GsxNn9SrapyMPpIjux4N/view?usp=sharing)

## 🎯 Features

### ⏰ **Smart Reminders**
- **Custom Scheduling** - Set reminders for any date and time
- **Rich Notifications** - Interactive notification system

### 📝 **Reminder Management**
- **Add/Edit Reminders** - Simple and intuitive reminder creation
- **Past Reminders** - View completed and expired reminders
- **Reminder Details** - Comprehensive reminder information view
- **Smart Organization** - Automatic categorization by date

### 🔔 **Advanced Notifications**
- **Persistent Notifications** - Never miss important reminders
- **Notification Channels** - Organized notification management

### 🎨 **Modern UI/UX**
- **Material 3 Design** - Latest Google design language
- **Dynamic Theming** - Light/Dark mode with system preference
- **Responsive Design** - Optimized for all screen sizes

## 🏗️ Architecture

```
Clean Architecture + MVVM
├── Presentation Layer (Jetpack Compose + ViewModels)
├── Domain Layer (Use Cases, Models & Repository Interfaces)  
├── Data Layer (Repository Implementation, Firebase Setup, Caches, Managers)
└── DI Layer (Dependency Injection)
```

### 🛠️ **Tech Stack**
- **UI**: Jetpack Compose + Material 3
- **DI**: Dagger Hilt
- **Database**: Room Database
- **Preferences**: DataStore
- **Notifications**: AlarmManager + NotificationManager
- **Architecture**: MVVM + Repository Pattern
- **Authentication**: Custom Login System

## 📱 Screens

| Screen | Description |
|--------|-------------|
| **Login** | Secure user authentication |
| **Home** | Dashboard with upcoming reminders |
| **Add Reminder** | Create new reminders with custom settings |
| **Past Reminders** | View completed and expired reminders |
| **Reminder Detail** | Detailed view with edit/delete options |
| **Settings** | App preferences and theme configuration |

## 📱 App Screenshots

<div align="center">
  <table>
    <tr>
      <td align="center">
        <img src="https://github.com/prafullKrRj/Chronos/blob/master/images/Screenshot_20250725_152058.png?raw=true" alt="Login Screen" width="280" style="border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);"/>
        <br><strong>Login Screen</strong>
        <br><sub>Secure user authentication</sub>
      </td>
      <td align="center">
        <img src="https://github.com/prafullKrRj/Chronos/blob/master/images/Screenshot_20250725_142931.png?raw=true" alt="Home Screen" width="280" style="border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);"/>
        <br><strong>Home Screen</strong>
        <br><sub>Dashboard with upcoming reminders</sub>
      </td>
    </tr>
    <tr>
      <td align="center">
        <img src="https://github.com/prafullKrRj/Chronos/blob/master/images/Screenshot_20250725_165121.png?raw=true" alt="Add Reminder" width="280" style="border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);"/>
        <br><strong>Add Reminder</strong>
        <br><sub>Create new reminders easily</sub>
      </td>
      <td align="center">
        <img src="https://github.com/prafullKrRj/Chronos/blob/master/images/Screenshot_20250725_143042.png?raw=true" alt="Past Reminders" width="280" style="border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);"/>
        <br><strong>Past Reminders</strong>
        <br><sub>View completed reminders history</sub>
      </td>
    </tr>
    <tr>
      <td align="center">
        <img src="https://github.com/prafullKrRj/Chronos/blob/master/images/Screenshot_20250725_143112.png?raw=true" alt="Reminder Detail" width="280" style="border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);"/>
        <br><strong>Reminder Detail</strong>
        <br><sub>Detailed reminder information</sub>
      </td>
      <td align="center">
        <img src="https://github.com/prafullKrRj/Chronos/blob/master/images/Screenshot_20250725_143312.png?raw=true" alt="Notification" width="280" style="border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);"/>
        <br><strong>Notification</strong>
        <br><sub>Rich notification system</sub>
      </td>
    </tr>
    <tr>
      <td align="center">
        <img src="https://github.com/prafullKrRj/Chronos/blob/master/images/Screenshot_20250725_143347.png?raw=true" alt="Reminder from Notification" width="280" style="border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);"/>
        <br><strong>Reminder from Notification</strong>
        <br><sub>Quick access from notifications</sub>
      </td>
      <td align="center">
        <img src="https://github.com/prafullKrRj/Chronos/blob/master/images/settings.png?raw=true" alt="Settings" width="280" style="border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);"/>
        <br><strong>Settings</strong>
        <br><sub>App preferences & configuration</sub>
      </td>
    </tr>
  </table>
</div>

## 📂 Project Structure

```
app/src/main/java/com/prafullkumar/chronos/
├── core/
│   ├── Mapper.kt           # Data mapping utilities
│   ├── Resource.kt         # Network resource wrapper
│   ├── UiState.kt         # UI state management
│   └── Utils.kt           # Utility functions
├── data/
│   ├── cache/             # Caching mechanism
│   │   └── CacheManager.kt
│   ├── dtos/              # Data transfer objects
│   │   ├── ReminderDto.kt
│   │   └── UserDto.kt
│   ├── managers/          # System managers
│   │   ├── ChronosAlarmManager.kt
│   │   └── ChronosNotificationManager.kt
│   ├── mappers/           # Entity mappers
│   │   └── ReminderMapper.kt
│   ├── preferences/       # SharedPreferences
│   │   └── ThemePreferences.kt
│   ├── receiver/          # Broadcast receivers
│   │   └── AlarmReceiver.kt
│   └── repository/        # Repository implementations
│       └── HomeRepositoryImpl.kt
├── di/                    # Dependency injection
│   ├── Module.kt
│   └── NotificationModule.kt
├── domain/
│   ├── model/             # Domain models
│   │   └── Reminder.kt
│   └── repository/        # Repository interfaces
│       ├── AiRepository.kt
│       ├── HomeRepository.kt
│       ├── LoginRepository.kt
│       └── ReminderRepository.kt
└── presentation/
    ├── navigation/        # Navigation setup
    │   ├── AppNavigation.kt
    │   └── Routes.kt
    ├── screens/           # Compose screens
    │   ├── add/           # Add reminder screen
    │   ├── edit/          # Edit reminder screen
    │   ├── home/          # Home screen
    │   ├── onBoarding/    # Onboarding screens
    │   ├── past/          # Past reminders screen
    │   ├── reminder/      # Reminder detail screen
    │   ├── reminderFromNavigation/
    │   └── settings/      # Settings screen
    ├── ui/
    │   └── theme/         # Material 3 theming
    │       ├── Color.kt
    │       ├── Theme.kt
    │       ├── ThemeManager.kt
    │       └── Type.kt
    ├── ChronosApplication.kt
    └── MainActivity.kt
```

## 🎨 Design Highlights

- **Material 3 Design** - Modern and intuitive interface
- **Card-based Layout** - Clean content organization  
- **Smart Typography** - Proper text hierarchy and readability
- **Loading States** - Smooth loading animations
- **Error Handling** - User-friendly error messages with retry options
- **Responsive Design** - Optimized for different screen sizes

## 🔔 Notification System

**Key Features:**
```kotlin
// Alarm Management
ChronosAlarmManager - Schedule and manage reminders

// Notification Management  
ChronosNotificationManager - Handle notification display

// Broadcast Receiver
AlarmReceiver - Process alarm triggers

// Notification Channels
- High Priority Reminders
- Regular Reminders  
- System Notifications
```

## 💾 Data Management

- **Smart Caching** - Efficient data caching with CacheManager reducing internet calls.
- **DataStore Preferences** - Modern preference storage
- **FirebaseListner** - For real time updates.

## 🔐 Security Features

- **User Authentication** - Secure login system
- **Local Data Storage** - Data stays on device
- **Privacy First** - No external data transmission
- **Secure Preferences** - Encrypted user preferences

## 🔧 Core Components

- **Alarm System** - Precise reminder scheduling
- **Notification Manager** - Rich notification system
- **Theme Manager** - Dynamic theming support
- **Cache Manager** - Intelligent data caching
- **Repository Pattern** - Clean data architecture

## 👨‍💻 Developer

**Prafull Kumar**  
[GitHub](https://github.com/prafullKrRj) • [LinkedIn](https://linkedin.com/in/prafullkrRj)

---

<div align="center">
  <p>Built with ❤️ using Jetpack Compose</p>
  <p><sub>⭐ Star this repo if you found it helpful!</sub></p>
</div>
