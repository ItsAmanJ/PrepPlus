
<div align="center">
  <img src="https://readme-typing-svg.demolab.com?font=Fira+Code&weight=800&size=40&pause=1000&color=FF4500&center=true&vCenter=true&width=800&lines=Currently+In+Development+Phase..." alt="Currently in development phase" />
</div>

# PrepPulse 🚀

PrepPulse is a comprehensive, offline-first study management and productivity application designed to help students track their timetable, homework, and study sessions efficiently. 

## 📸 Screenshots

| Today & Dashboard | Timetable & OCR | Focus Timer | Analytics |
|:---:|:---:|:---:|:---:|
| <img src="screenshot_today.png" width="200"/> | <img src="screenshot_timetable.png" width="200"/> | <img src="screenshot_timer.png" width="200"/> | <img src="screenshot_analytics.png" width="200"/> |

## ✨ Features

- **Smart Timetable Management**: Automatically parse and import your timetable from images or text using AI (Gemini OCR), or add classes manually.
- **Homework & Task Tracker**: Keep track of your assignments with subjects, due dates, priority levels, and custom tags.
- **Focus Timer**: Boost your productivity with a built-in Pomodoro timer and stopwatch mode. Includes ambient sound options for deep work.
- **Study Analytics**: Visualize your progress with detailed analytics, including total study hours, task completion rates, streaks, and subject-wise breakdowns.
- **Offline First**: All your data is securely stored on your device using a local Room Database. 
- **Modern UI/UX**: Built with Jetpack Compose featuring a sleek, high-density dark theme and Material 3 design guidelines.

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose (Material 3)
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture principles
- **Local Storage**: Room Database + Kotlin Coroutines & Flow
- **AI Integration**: Gemini API for Timetable OCR parsing
- **CI/CD**: GitHub Actions for automated APK builds

## 🚀 How to Download & Install (No PC Required)

If you have uploaded this project to GitHub, you can download the APK directly from the GitHub Actions tab using your phone:

1. Open your GitHub repository in your phone's mobile browser.
2. Go to the **Actions** tab.
3. Click on the latest successful workflow run for **Build Android APK**.
4. Scroll down to the **Artifacts** section.
5. Tap on **PrepPulse-Debug-APK** to download the zip file.
6. Extract the zip file using a file manager app on your phone.
7. Tap the extracted `.apk` file to install it. *(Note: You may need to enable "Install unknown apps" in your Android settings).*

## 💻 Local Development Setup

To run this project locally on your machine using Android Studio:

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/PrepPulse.git
   ```
2. Open the project in Android Studio.
3. Create a `.env` file in the root directory and add your Gemini API key:
   ```env
   GEMINI_API_KEY="your_api_key_here"
   ```
4. Sync the project with Gradle files.
5. Build and run the app on an emulator or physical device.

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! Feel free to check the issues page.

## 📝 License

This project is open-source and available under the [MIT License](LICENSE).
