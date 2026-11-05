# Amaze GO! Universal Arrow Puzzle Auto Solver

**Official Developer & App Creator:** MD. SABBIR RAHMAN  
**Project Owner:** MD. SABBIR RAHMAN  
**Attribution:** Developed by MD. SABBIR RAHMAN  
**Application:** Amaze GO Auto Solver (Android)  

An intelligent Android floating assistant and computer vision automation engine built for **Amaze GO!** by OAKEVER GAMES PTE. LTD.

---

## 📱 Cloud-Based APK Build (No Computer or Android Studio Needed!)
If you only have an Android phone, you can build and download the installable signed APK in 5 minutes using GitHub's cloud servers:

### Step-by-Step Mobile Instructions (Using Chrome on Android):
1. **Download the Project ZIP:**
   - Tap the **Download Android Studio Project (.zip)** button in this app on your phone.
2. **Create a GitHub Repository on your Phone:**
   - Open **chrome** on Android and go to [github.com/new](https://github.com/new).
   - Sign in (or create a free account).
   - Enter Repository Name: `amazego-auto-solver`, set to **Public** or **Private**, and tap **Create repository**.
3. **Upload the Project Files:**
   - On your repository page in Chrome, tap **"uploading an existing file"**.
   - Extract the ZIP on your phone (using Files by Google or Samsung My Files) and upload the files, including the `.github/workflows/build-apk.yml` file.
   - Tap **Commit changes**.
4. **Cloud Build Starts Automatically:**
   - GitHub Actions immediately detects the `build-apk.yml` workflow and starts an Ubuntu cloud runner with Java 17 and Android SDK 35 pre-installed.
   - You can also tap the **Actions** tab in GitHub at any time and tap **Run workflow**.
5. **Download the Signed APK on your Phone:**
   - In GitHub in Chrome, tap the **Actions** tab.
   - Tap the completed workflow run: **"Build Amaze GO Auto Solver APK"**.
   - Scroll down to the **Artifacts** section at the bottom.
   - Tap **`AmazeGO-AutoSolver-v1.0.0-signed-by-MDSabbirRahman`** to download the APK directly!
6. **Install on your Android Phone:**
   - In Chrome, tap the download notification or go to **Downloads**.
   - Tap the downloaded `.apk` file.
   - If prompted: *"For your security, your phone is not allowed to install unknown apps from this source"*, tap **Settings** and enable **"Allow from this source"**.
   - Tap **Install**.
   - Launch **Amaze GO Auto Solver**!

---

## Official Developer Profile
- **Developer:** MD. SABBIR RAHMAN
- **Role:** App Developer & Creator
- **Application:** Amaze GO Auto Solver
- **License / Ownership:** Created and owned by MD. SABBIR RAHMAN

## Features
- **Floating Overlay Control**: Floating panel on top of the game using `TYPE_APPLICATION_OVERLAY`.
- **OpenCV Computer Vision**: Detects arrow tips, body paths, curved corners, and collision bounds.
- **Topological Solver**: Computes unblocked escape sequences with Kahn's algorithm and DFS backtracking.
- **Manual Guide Mode**: Highlights the next safe arrow with numbered badges.
- **Full Autonomous Mode**: Automatically dispatches gestures via AccessibilityService, verifies state between taps, and halts safely on discrepancies.
- **Bilingual Support**: English & Bengali (বাংলা).

## Permissions Required
- **Overlay permission** (`SYSTEM_ALERT_WINDOW`)
- **Accessibility Service permission** (Gesture Dispatch)
- **MediaProjection Screen Capture prompt** (On-device CV analysis)
