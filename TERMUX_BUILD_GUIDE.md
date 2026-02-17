# Building ZipSigner in Termux (Android)

This guide shows how to compile the ZipSigner app directly on your Android phone using Termux.

---

## ⚠️ Important Notes

**Challenges with Termux Build:**
- Building Android apps in Termux is possible but has limitations
- Requires ~3-4 GB of free storage
- Takes significant time on mobile devices (30-60 minutes)
- Android SDK setup in Termux is complex
- **Recommendation**: Use GitHub Actions instead (see other guide)

**If you still want to build in Termux, follow these steps:**

---

## Prerequisites

- **Android Device** with at least 4GB free storage
- **Termux** app installed from F-Droid (NOT Google Play version)
- **Stable internet connection**
- **Patience** - initial setup takes 30-60 minutes

---

## Step 1: Install Termux

Download Termux from F-Droid (the Play Store version is outdated):
- Go to https://f-droid.org/
- Search for "Termux"
- Install Termux and Termux:API

---

## Step 2: Setup Termux Environment

Open Termux and run these commands:

```bash
# Update packages
pkg update && pkg upgrade -y

# Install essential packages
pkg install -y wget curl git unzip zip

# Install Java JDK 17
pkg install -y openjdk-17

# Verify Java installation
java -version
```

You should see Java 17.x.x

---

## Step 3: Setup Storage Access

```bash
# Allow Termux to access phone storage
termux-setup-storage

# Grant permission when prompted

# Navigate to a working directory
cd ~/storage/downloads
mkdir zipsigner-build
cd zipsigner-build
```

---

## Step 4: Extract Your Project

```bash
# Copy your zip file to Downloads folder on your phone
# Then extract it in Termux:

unzip ~/storage/downloads/zip-signer-modernized-v4.0.zip
cd zip-signer-updated
```

---

## Step 5: Install Android SDK (The Hard Part)

**Option A: Use Command-line Tools (Recommended)**

```bash
# Create SDK directory
mkdir -p ~/android-sdk
cd ~/android-sdk

# Download Android command-line tools
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip

# Extract
unzip commandlinetools-linux-11076708_latest.zip

# Create proper directory structure
mkdir -p cmdline-tools/latest
mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true

# Set environment variables
export ANDROID_HOME=~/android-sdk
export ANDROID_SDK_ROOT=~/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools

# Save to .bashrc for future sessions
echo "export ANDROID_HOME=~/android-sdk" >> ~/.bashrc
echo "export ANDROID_SDK_ROOT=~/android-sdk" >> ~/.bashrc
echo "export PATH=\$PATH:\$ANDROID_HOME/cmdline-tools/latest/bin" >> ~/.bashrc
echo "export PATH=\$PATH:\$ANDROID_HOME/platform-tools" >> ~/.bashrc

# Accept licenses
yes | sdkmanager --licenses

# Install required SDK components
sdkmanager "platform-tools"
sdkmanager "platforms;android-35"
sdkmanager "build-tools;35.0.0"
```

This will download ~1-2 GB of SDK files.

---

## Step 6: Create local.properties

```bash
# Navigate to your project
cd ~/storage/downloads/zipsigner-build/zip-signer-updated

# Create local.properties file
cat > local.properties << EOF
sdk.dir=$HOME/android-sdk
EOF
```

---

## Step 7: Build the Project

```bash
# Make gradlew executable
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# This will take 20-40 minutes on first build
# Subsequent builds are faster (5-10 minutes)
```

---

## Step 8: Find Your APK

If build succeeds:

```bash
# APK location
ls -lh ZipSigner/build/outputs/apk/debug/

# Copy to Downloads for easy access
cp ZipSigner/build/outputs/apk/debug/*.apk ~/storage/downloads/
```

Your APK will be in your phone's Downloads folder!

---

## Common Issues & Solutions

### Issue 1: Out of Memory
```bash
# Error: Java heap space
# Solution: Reduce memory usage in gradle.properties
echo "org.gradle.jvmargs=-Xmx1536m" >> gradle.properties
```

### Issue 2: SDK Not Found
```bash
# Error: SDK location not found
# Solution: Check ANDROID_HOME is set
echo $ANDROID_HOME
# Should show: /data/data/com.termux/files/home/android-sdk

# If not set, run:
export ANDROID_HOME=~/android-sdk
```

### Issue 3: Build Tools Missing
```bash
# Error: Build-Tools X.X.X not found
# Solution: Install specific version
sdkmanager "build-tools;35.0.0"
```

### Issue 4: Gradle Daemon Issues
```bash
# Error: Gradle daemon disappeared
# Solution: Disable daemon
./gradlew assembleDebug --no-daemon
```

### Issue 5: Network Timeouts
```bash
# Error: Could not download dependencies
# Solution: Use mobile data or different WiFi
# Or increase timeout in gradle.properties:
echo "systemProp.http.socketTimeout=300000" >> gradle.properties
echo "systemProp.http.connectionTimeout=300000" >> gradle.properties
```

---

## Storage Space Requirements

- **Initial Setup**: ~3-4 GB
  - Android SDK: ~1.5 GB
  - Gradle cache: ~500 MB
  - Dependencies: ~300 MB
  - Build outputs: ~200 MB
  - Source code: ~20 MB

- **After First Build**: Keep ~2 GB free for rebuilds

---

## Performance Tips

1. **Close other apps** before building
2. **Use WiFi** for faster downloads
3. **Keep phone charging** during build
4. **Don't let screen timeout** (prevents Termux suspension)
5. **Acquire wakelock**: Run `termux-wake-lock` before building

---

## Alternative: Simplified Build (No SDK)

If you just need to modify code without Android SDK:

```bash
# Build only Java modules (not the Android app)
./gradlew :zipsigner-lib:build
./gradlew :zipio-lib:build
```

This won't produce an APK but validates Java code.

---

## Clean Build (If You Need to Restart)

```bash
# Clean everything
./gradlew clean

# Delete gradle cache
rm -rf ~/.gradle/caches/

# Rebuild
./gradlew assembleDebug
```

---

## Estimated Times (On Modern Phone)

- **Initial Setup**: 30-45 minutes
- **First Build**: 20-40 minutes  
- **Subsequent Builds**: 5-10 minutes
- **Clean Build**: 15-25 minutes

---

## Why GitHub Actions is Better

**Termux Challenges:**
- ❌ Requires lots of storage
- ❌ Long build times
- ❌ Complex SDK setup
- ❌ Battery drain
- ❌ Can fail if connection drops

**GitHub Actions Advantages:**
- ✅ No storage needed on phone
- ✅ Fast build (5-10 minutes)
- ✅ Automatic setup
- ✅ No battery usage
- ✅ Reliable builds
- ✅ Free for public repos

**See the GitHub Actions guide for the easier method!**

---

## Success!

If everything worked, you'll have:
- `ZipSigner-debug.apk` in your Downloads folder
- Ready to install and use

Install it:
```bash
# Open in file manager and tap to install
# Or use adb if you have it:
termux-open ~/storage/downloads/ZipSigner-debug.apk
```

---

## Troubleshooting Resources

If you get stuck:
1. Check error messages carefully
2. Search error on Stack Overflow
3. Verify ANDROID_HOME is set correctly
4. Ensure you have enough storage
5. Try building with `--stacktrace` flag for details:
   ```bash
   ./gradlew assembleDebug --stacktrace
   ```

---

**Good luck! But seriously, consider using GitHub Actions instead! 😊**
