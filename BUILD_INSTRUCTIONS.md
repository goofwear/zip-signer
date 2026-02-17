# Quick Start Guide - ZipSigner Updated

## For Users - Installing the Updated App

### Option 1: Build from Source (Recommended)
Follow the instructions in the "Building from Source" section below.

### Option 2: Using Pre-built APK
If an APK is provided:
1. Enable "Install from Unknown Sources" on your device
2. Transfer the APK to your device
3. Install the APK
4. Grant storage permissions when prompted

---

## For Developers - Building from Source

### System Requirements

**Required:**
- Operating System: Windows 10+, macOS 10.14+, or Linux
- Android Studio: Hedgehog (2023.1.1) or newer
- JDK: Version 17 (OpenJDK or Oracle JDK)
- Minimum 8GB RAM (16GB recommended)
- 10GB free disk space

**Recommended:**
- Android Studio: Latest stable version
- Latest Android SDK Platform Tools
- Git (for version control)

### Step-by-Step Build Instructions

#### 1. Install Android Studio
Download from: https://developer.android.com/studio

#### 2. Install JDK 17
**On Windows:**
- Download from: https://adoptium.net/
- Install and set JAVA_HOME environment variable

**On macOS:**
```bash
brew install openjdk@17
```

**On Linux:**
```bash
sudo apt install openjdk-17-jdk  # Debian/Ubuntu
sudo yum install java-17-openjdk  # RHEL/CentOS
```

#### 3. Open Project in Android Studio
1. Launch Android Studio
2. Click "Open" or "File -> Open"
3. Navigate to the `zip-signer-updated` folder
4. Click "OK"

#### 4. SDK Setup
When prompted, Android Studio will:
- Download required SDK platforms (API 35)
- Download build tools
- Sync Gradle files

If not automatic:
1. Go to "Tools -> SDK Manager"
2. Install "Android 15.0 (API 35)"
3. Go to "SDK Tools" tab
4. Ensure these are installed:
   - Android SDK Build-Tools
   - Android SDK Platform-Tools
   - Android Emulator (optional, for testing)

#### 5. Gradle Sync
Android Studio should automatically sync. If issues occur:
1. Click "File -> Sync Project with Gradle Files"
2. Wait for sync to complete
3. Check "Build" window for errors

#### 6. Build the App

**For Debug Build:**
1. Select "Build -> Build Bundle(s) / APK(s) -> Build APK(s)"
2. Wait for build to complete
3. Click "locate" when build finishes
4. APK location: `ZipSigner/build/outputs/apk/debug/`

**For Release Build:**
1. Create a keystore if you don't have one:
   - Build -> Generate Signed Bundle / APK
   - Create new keystore
   - Fill in details and remember passwords
2. Build -> Generate Signed Bundle / APK
3. Select "APK"
4. Choose your keystore
5. Select "release" build variant
6. Click "Finish"
7. APK location: `ZipSigner/build/outputs/apk/release/`

**Command Line Build:**
```bash
# Navigate to project root
cd zip-signer-updated

# For debug build
./gradlew assembleDebug

# For release build (unsigned)
./gradlew assembleRelease

# Clean and rebuild
./gradlew clean assembleDebug
```

**On Windows:**
```cmd
gradlew.bat assembleDebug
```

---

## Troubleshooting

### Common Issues and Solutions

#### Issue: "SDK location not found"
**Solution:**
Create `local.properties` file in project root:
```properties
sdk.dir=/path/to/Android/Sdk
```

**Find SDK path:**
- Windows: `C:\Users\<username>\AppData\Local\Android\Sdk`
- macOS: `/Users/<username>/Library/Android/sdk`
- Linux: `/home/<username>/Android/Sdk`

#### Issue: "Java version mismatch"
**Solution:**
1. Verify JDK 17 is installed: `java -version`
2. Set JAVA_HOME in Android Studio:
   - File -> Settings -> Build, Execution, Deployment -> Build Tools -> Gradle
   - Set "Gradle JDK" to "17"

#### Issue: "Gradle sync failed"
**Solution:**
1. Check internet connection (Gradle downloads dependencies)
2. File -> Invalidate Caches -> Invalidate and Restart
3. Delete `.gradle` folder in project root
4. Sync again

#### Issue: "Build failed - dependencies not found"
**Solution:**
Ensure repositories are accessible:
1. Check build.gradle has `google()` and `mavenCentral()`
2. Check internet/firewall settings
3. Try with VPN if in restricted network

#### Issue: "Out of memory during build"
**Solution:**
Edit `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
```

#### Issue: "Android SDK not found"
**Solution:**
1. Open SDK Manager (Tools -> SDK Manager)
2. Install Android 15.0 (API 35)
3. Install latest build tools
4. Sync project again

---

## Testing the App

### On Emulator
1. Tools -> Device Manager
2. Create Virtual Device
3. Choose a device (e.g., Pixel 6)
4. Select system image: Android 15 (API 35)
5. Click "Finish"
6. Run the app: Run -> Run 'app'

### On Physical Device
1. Enable Developer Options on device:
   - Settings -> About Phone
   - Tap "Build Number" 7 times
2. Enable USB Debugging:
   - Settings -> Developer Options
   - Enable "USB Debugging"
3. Connect device via USB
4. Accept debugging prompt on device
5. Run -> Run 'app'
6. Select your device

### Testing Storage Permissions
The app requires storage access. When testing:
1. First launch will request permissions
2. Grant "Allow access to all files" (Android 11+)
3. Or grant "Read/Write Storage" (Android 10 and below)

---

## Project Structure

```
zip-signer-updated/
├── ZipSigner/                    # Main Android app module
│   ├── src/
│   │   └── main/
│   │       ├── java/             # Java source code
│   │       ├── res/              # Resources (layouts, strings, etc.)
│   │       └── AndroidManifest.xml
│   ├── build.gradle              # App module build config
│   └── proguard-rules.pro        # ProGuard/R8 rules
├── android-sun-jarsign-support/  # JAR signing support library
├── kellinwood-logging-*/         # Logging libraries
├── zipio-lib/                    # ZIP I/O library
├── zipsigner-lib/                # Core signing library
├── zipsigner-lib-optional/       # Optional signing features
├── build.gradle                  # Root build configuration
├── settings.gradle               # Project modules configuration
├── gradle.properties             # Gradle properties
└── README_MODERNIZATION.md       # Detailed change log
```

---

## Configuration Files

### gradle.properties
Controls build behavior:
- JVM memory allocation
- AndroidX enablement
- Build optimizations
- Module versions

### build.gradle (root)
Defines:
- Build script dependencies
- Common repositories
- Sub-project configurations

### ZipSigner/build.gradle
Configures:
- Android SDK versions
- Dependencies
- Build types (debug/release)
- ProGuard settings

---

## Next Steps After Building

### For Distribution
1. Test thoroughly on multiple Android versions
2. Sign release APK with your production keystore
3. Optimize APK size (already enabled with R8)
4. Generate signed AAB for Play Store (if publishing)

### For Development
1. Set up version control (Git)
2. Configure code signing for CI/CD
3. Implement automated testing
4. Consider migrating to Kotlin for new features

---

## Additional Help

### Documentation
- [Android Developer Guide](https://developer.android.com/guide)
- [Gradle Build Guide](https://docs.gradle.org/current/userguide/userguide.html)
- [AndroidX Migration](https://developer.android.com/jetpack/androidx/migrate)

### Support
- Check README_MODERNIZATION.md for detailed changes
- Review original ZipSigner documentation
- Android Studio Help -> Submit Feedback

---

## Security Notes

### Code Signing
This app is used to sign other apps. Security considerations:
- Keep your keystores secure
- Use strong passwords
- Never commit keystores to version control
- Backup keystores securely

### Permissions
The app requests MANAGE_EXTERNAL_STORAGE which:
- Is required for signing files anywhere on device
- Requires user approval on Android 11+
- May require justification for Play Store submission

---

## Build Variants

### Debug
- Includes debug symbols
- Not optimized
- Debuggable
- Use for: Development and testing

### Release
- Optimized with R8
- No debug symbols
- Requires signing
- Use for: Production distribution

---

**Happy Building! 🚀**
