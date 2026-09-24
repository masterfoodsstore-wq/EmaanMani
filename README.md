# Dragon vs Tiger - Android Arcade Suite

A native Android game suite featuring Zoo Roulette 3D and Dragon vs. Tiger card duel, built with Kotlin and Jetpack Compose.

---

## 📋 Table of Contents
1. [Prerequisites](#1-prerequisites)
2. [How to Create a GitHub Repository](#2-how-to-create-a-github-repository)
3. [How to Upload & Push This Project to GitHub](#3-how-to-upload--push-this-project-to-github)
4. [How the GitHub Actions Workflow Works & How to Run It](#4-how-the-github-actions-workflow-works--how-to-run-it)
5. [Where to Find the Generated APK](#5-where-to-find-the-generated-apk)
6. [How to Download the APK from GitHub Actions](#6-how-to-download-the-apk-from-github-actions)
7. [How to Build the APK Locally with Android Studio](#7-how-to-build-the-apk-locally-with-android-studio)
8. [How to Create a Proper Release Signing Key (Production)](#8-how-to-create-a-proper-release-signing-key-production)
9. [Project Structure](#9-project-structure)

---

## 1. Prerequisites

- **Git** installed on your computer (`git --version`)
- A **GitHub** account ([github.com](https://github.com))
- (Optional for local development) **Android Studio Ladybug / Koala** or newer with JDK 21 and Android SDK 36.

---

## 2. How to Create a GitHub Repository

1. Open your browser and log in to [GitHub](https://github.com).
2. Click the **+** (plus icon) in the top-right corner of the page and select **New repository**.
3. Choose a repository name (for example, `dragon-vs-tiger` or `my-game-app`).
4. Set the visibility to **Public** or **Private** based on your preference.
5. **IMPORTANT**: Do **NOT** check "Add a README file", "Add .gitignore", or "Choose a license" (leave them unchecked since this project already includes them).
6. Click **Create repository**.
7. Copy the repository URL (e.g. `https://github.com/<YOUR_USERNAME>/<REPO_NAME>.git`).

---

## 3. How to Upload & Push This Project to GitHub

Open a terminal or command prompt inside the root directory of this project and run the following exact Git commands:

```bash
# 1. Initialize git if not already initialized
git init

# 2. Stage all project files (safe .gitignore protects all credentials)
git add .

# 3. Create your initial commit
git commit -m "Initial commit: GitHub-ready Android project with CI/CD"

# 4. Set the primary branch to main
git branch -M main

# 5. Add your GitHub remote repository (replace with your actual GitHub URL)
git remote add origin https://github.com/<YOUR_USERNAME>/<REPO_NAME>.git

# 6. Push code to GitHub
git push -u origin main
```

> **Note for authentication**: When prompted for your password during `git push`, use a **GitHub Personal Access Token (PAT)** with `repo` and `workflow` scopes. (Generate one in GitHub: *Settings -> Developer settings -> Personal access tokens -> Tokens (classic)*).

---

## 4. How the GitHub Actions Workflow Works & How to Run It

The automated build workflow is configured in `.github/workflows/build-release-apk.yml`.

### Automatic Trigger
Every time you `git push` code changes to the `main` branch, GitHub Actions automatically:
1. Provisions an Ubuntu 24.04 runner with **Java 21 (Temurin)** and the runner's pre-installed **Android SDK**.
2. Prepares the Gradle wrapper and executes:
   ```bash
   ./gradlew assembleRelease --stacktrace
   ```
3. Verifies that the release APK is created at:
   ```
   app/build/outputs/apk/release/app-release.apk
   ```
4. Uploads the generated APK as a downloadable **GitHub Actions artifact** named `ERAN-MONEY-APK`.
5. Publishes or updates the GitHub Release titled **"ERAN MONEY"** with `ERAN-MONEY-APK.apk` directly attached under **Assets**.

### Manual Trigger (Workflow Dispatch)
You can also trigger a build manually anytime without making a commit:
1. Navigate to your repository on GitHub.
2. Click the **Actions** tab at the top.
3. In the left sidebar, click **Build Release APK**.
4. Click the **Run workflow** dropdown on the right side.
5. Enter or confirm the release tag (e.g. `v1.0.0`) and click **Run workflow**.

---

## 5. Where to Find the Generated APK

### In GitHub Releases (Direct .apk Download)
- Navigate to the **Releases** page of your GitHub repository.
- Under the **ERAN MONEY** release, look under the **Assets** section.
- Click on **`ERAN-MONEY-APK.apk`** to download the APK file directly to your phone or computer.

### In GitHub Actions (Workflow Artifacts)
- **Workflow Run Artifacts**: Under the **Artifacts** section of the completed workflow run, named **`ERAN-MONEY-APK`**.

### In Local Builds
When built on your computer or local machine, the APK is located at:
```
app/build/outputs/apk/release/app-release.apk
```

---

## 6. How to Download the APK from GitHub Actions

1. Go to your repository on [GitHub](https://github.com).
2. Click on the **Releases** tab on the right side (or **Actions** tab).
3. Under the **ERAN MONEY** release, scroll to **Assets**.
4. Click **`ERAN-MONEY-APK.apk`** to download it directly without needing to unzip anything!
5. Install `ERAN-MONEY-APK.apk` on your Android phone!

---

## 7. How to Build the APK Locally with Android Studio

### Using Android Studio GUI:
1. Open **Android Studio**.
2. Select **Open** and choose the root folder of this project.
3. Wait for Gradle sync to complete.
4. From the top menu bar, click **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
5. Once complete, click the **locate** popup notification to open the output folder containing the APK.

### Using the Command Line:
Open a terminal in the project root:

- **Linux / macOS**:
  ```bash
  ./gradlew assembleRelease
  ```
- **Windows (Command Prompt / PowerShell)**:
  ```bat
  gradlew.bat assembleRelease
  ```

Your APK will be ready at:
```
app/build/outputs/apk/release/app-release.apk
```

---

## 8. How to Create a Proper Release Signing Key (Production)

For distributing on Google Play Store or installing as a permanent verified release, you can generate your own private production keystore:

### Step 1: Generate a Keystore File
Run this command in your terminal (replace `my-release-key.jks` and `my-alias`):
```bash
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```
You will be prompted to enter and confirm passwords and certificate details.

### Step 2: Configure Local Signing (Protected by .gitignore)
Create a file named `release-keystore.properties` in the project root directory (this file is strictly ignored by `.gitignore` so your credentials are never pushed to GitHub):

```properties
storeFile=/absolute/path/to/my-release-key.jks
storePassword=YourKeystorePassword
keyAlias=my-key-alias
keyPassword=YourKeyPassword
```

### Step 3: Configure GitHub Actions Production Signing (via GitHub Secrets)
To sign your production APK in GitHub Actions without committing keys:
1. Encode your `.jks` file to Base64:
   ```bash
   base64 -w 0 my-release-key.jks > keystore_base64.txt
   ```
2. In your GitHub repository, go to **Settings** > **Secrets and variables** > **Actions**.
3. Add the following repository secrets:
   - `RELEASE_KEYSTORE_BASE64`: Paste the content of `keystore_base64.txt`.
   - `RELEASE_STORE_PASSWORD`: Keystore password.
   - `RELEASE_KEY_ALIAS`: Key alias.
   - `RELEASE_KEY_PASSWORD`: Key password.

The build script will automatically detect and apply the production key if present, falling back to standard signing if not provided.

---

## 9. Project Structure

```
├── .github/
│   └── workflows/
│       └── build-apk.yml          # GitHub Actions CI/CD release workflow
├── app/
│   ├── build.gradle.kts           # App-level build configuration & release signing
│   ├── proguard-rules.pro         # Proguard rules for release optimization
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/example/  # Application code (Jetpack Compose UI & models)
│           └── res/               # Drawables, mipmaps, strings, layouts
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── .gitignore                     # Protects keystores, build artifacts & secrets
├── build.gradle.kts               # Root build script
├── gradle.properties              # JVM memory & Android build flags
├── gradlew                        # Unix Gradle wrapper executable
├── gradlew.bat                    # Windows Gradle wrapper executable
├── metadata.json                  # AI Studio platform configuration
├── settings.gradle.kts            # Project repositories & module definitions
└── README.md                      # Documentation & GitHub setup guide
```
