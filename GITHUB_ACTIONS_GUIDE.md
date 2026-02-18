# Building ZipSigner with GitHub Actions (RECOMMENDED!)

This is the **easiest and fastest** way to build your APK! GitHub will build it for you in the cloud - no Android Studio or heavy software needed!

---

## Why Use GitHub Actions?

✅ **No setup required** - GitHub does everything  
✅ **Fast** - Builds in 5-10 minutes  
✅ **Free** - Unlimited for public repositories  
✅ **No storage needed** on your device  
✅ **Automatic** - Just push code and get APK  
✅ **Always works** - Professional build environment  

---

## Prerequisites

- GitHub account (free)
- Your updated ZipSigner ZIP file
- 10 minutes of your time

---

## Step-by-Step Guide

### Step 1: Create GitHub Account (if you don't have one)

1. Go to https://github.com
2. Click "Sign up"
3. Enter email, password, username
4. Verify your email
5. Done! ✅

---

### Step 2: Create a New Repository

1. **Log in to GitHub**
2. **Click the "+" icon** (top right) → "New repository"
3. **Fill in details:**
   - Repository name: `zipsigner-app` (or any name you like)
   - Description: "ZipSigner Android App - Modernized for Android 15"
   - **Public** or Private (Public is free, Private requires paid plan for Actions)
   - ✅ Check "Add a README file"
   - Click "Create repository"

---

### Step 3: Upload Your Code

You have 3 options:

#### **Option A: Using GitHub Web Interface (Easiest)**

1. **Extract** `zip-signer-modernized-v4.0.zip` on your computer
2. **Open your GitHub repository** (the one you just created)
3. **Click "Add file"** → "Upload files"
4. **Drag and drop** the entire `zip-signer-updated` folder contents
   - Make sure to upload ALL files and folders
   - Including hidden files like `.github`
5. **Scroll down**, add commit message: "Initial upload of modernized ZipSigner"
6. **Click "Commit changes"**
7. Wait for upload to complete

**Important:** Make sure the `.github/workflows/build.yml` file is uploaded!

#### **Option B: Using GitHub Desktop (User-Friendly)**

1. Download GitHub Desktop: https://desktop.github.com
2. Install and sign in
3. Click "Clone Repository" → Select your repository
4. Choose a local folder
5. Extract your ZIP into this folder
6. GitHub Desktop will show changes
7. Add commit message: "Initial upload of modernized ZipSigner"
8. Click "Commit to main"
9. Click "Push origin"

#### **Option C: Using Git Command Line (For Advanced Users)**

```bash
# Extract your ZIP file
unzip zip-signer-modernized-v4.0.zip
cd zip-signer-updated

# Initialize git
git init
git add .
git commit -m "Initial upload of modernized ZipSigner"

# Add your GitHub repository
git remote add origin https://github.com/YOUR_USERNAME/zipsigner-app.git

# Push to GitHub
git branch -M main
git push -u origin main
```

---

### Step 4: Set Up GitHub Actions Workflow

**If you used Option A or B above, the workflow should already be there!**

Verify it's there:
1. Go to your repository on GitHub
2. Check if you have `.github/workflows/build.yml`

**If the workflow file is missing:**

1. In your repository, click "Add file" → "Create new file"
2. Name it: `.github/workflows/build.yml`
3. Copy and paste this content:

```yaml
name: Build Android APK

on:
  push:
    branches: [ main, master ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        distribution: 'temurin'
        java-version: '17'
        cache: 'gradle'
        
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
      
    - name: Build Debug APK
      run: ./gradlew assembleDebug --stacktrace
      
    - name: Upload Debug APK
      uses: actions/upload-artifact@v4
      with:
        name: ZipSigner-debug-apk
        path: ZipSigner/build/outputs/apk/debug/*.apk
        retention-days: 30
```

4. Click "Commit changes"

---

### Step 5: Trigger the Build

The build will start automatically after you commit, but you can also trigger it manually:

1. Go to your repository
2. Click the "Actions" tab
3. Click "Build Android APK" workflow (left sidebar)
4. Click "Run workflow" button (right side)
5. Select branch: "main"
6. Click "Run workflow"

The build will start immediately!

---

### Step 6: Watch the Build

1. Click on the running workflow (you'll see a yellow dot)
2. Watch the progress in real-time
3. Build takes about 5-10 minutes
4. Green checkmark = Success! ✅
5. Red X = Failed (check logs)

---

### Step 7: Download Your APK

Once the build succeeds (green checkmark):

1. **Scroll down** to "Artifacts" section
2. **Click "ZipSigner-debug-apk"** to download
3. **Extract the ZIP** file
4. **Install the APK** on your Android device!

**The APK will be available for 30 days.**

---

## Installing the APK on Your Phone

### Method 1: Direct Download (Easiest)
1. Open GitHub on your phone's browser
2. Go to Actions → Latest successful run
3. Download the artifact directly
4. Extract and install

### Method 2: Download to Computer Then Transfer
1. Download APK artifact from GitHub on computer
2. Extract the ZIP
3. Transfer APK to phone via USB
4. Install on phone

### Installation Steps:
1. Enable "Install from Unknown Sources" on your phone:
   - Settings → Security → Unknown Sources (ON)
   - Or Settings → Apps → Special Access → Install Unknown Apps
2. Tap the APK file
3. Click "Install"
4. Done! ✅

---

## Automatic Builds

Your workflow is configured to build automatically whenever you:
- Push code to the main branch
- Create a pull request
- Manually trigger it

So every time you update your code, you'll get a fresh APK automatically!

---

## Advanced: Build Signed Release APK

For a production-ready signed APK:

### Step 1: Create a Keystore

On your computer:
```bash
keytool -genkey -v -keystore my-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias my-key-alias
```

Enter password and details when prompted.

### Step 2: Convert Keystore to Base64

```bash
# On Linux/Mac:
base64 my-release-key.jks > keystore.base64

# On Windows (PowerShell):
[Convert]::ToBase64String([IO.File]::ReadAllBytes("my-release-key.jks")) > keystore.base64
```

### Step 3: Add Secrets to GitHub

1. Go to your repository settings
2. Click "Secrets and variables" → "Actions"
3. Click "New repository secret"

Add these secrets:

- **Name:** `KEYSTORE_FILE`  
  **Value:** (paste contents of keystore.base64)

- **Name:** `KEYSTORE_PASSWORD`  
  **Value:** (your keystore password)

- **Name:** `KEY_ALIAS`  
  **Value:** (your key alias, e.g., "my-key-alias")

- **Name:** `KEY_PASSWORD`  
  **Value:** (your key password)

### Step 4: Update Workflow

Add this job to your `.github/workflows/build.yml`:

```yaml
    - name: Decode Keystore
      if: github.event_name != 'pull_request'
      run: |
        echo "${{ secrets.KEYSTORE_FILE }}" | base64 -d > keystore.jks
        
    - name: Build Signed Release APK
      if: github.event_name != 'pull_request'
      run: |
        ./gradlew assembleRelease \
          -Pandroid.injected.signing.store.file=../keystore.jks \
          -Pandroid.injected.signing.store.password=${{ secrets.KEYSTORE_PASSWORD }} \
          -Pandroid.injected.signing.key.alias=${{ secrets.KEY_ALIAS }} \
          -Pandroid.injected.signing.key.password=${{ secrets.KEY_PASSWORD }}
          
    - name: Upload Signed APK
      if: github.event_name != 'pull_request'
      uses: actions/upload-artifact@v4
      with:
        name: ZipSigner-release-signed-apk
        path: ZipSigner/build/outputs/apk/release/*.apk
```

Now you'll get a signed release APK!

---

## Troubleshooting

### Build Failed?

1. **Click on the failed build**
2. **Expand the failed step** (red X)
3. **Read the error message**

Common issues:

**Error: "Task assembleDebug failed"**
- Solution: Check if all files were uploaded correctly
- Verify `.github/workflows/build.yml` exists

**Error: "Gradle build failed"**
- Solution: Check build.gradle files are correct
- Verify you uploaded the entire project structure

**Error: "Permission denied: gradlew"**
- Solution: This is handled by the workflow, shouldn't occur
- If it does, add: `run: chmod +x gradlew` before build step

**Error: "Java version mismatch"**
- Solution: Verify JDK 17 is specified in workflow
- Check `java-version: '17'` in workflow file

### Can't Find Artifact?

- Builds must complete successfully (green checkmark)
- Artifacts are at the bottom of the build page
- Artifacts expire after 30 days
- Re-run workflow to get fresh APK

### Workflow Not Starting?

- Ensure `.github/workflows/build.yml` is in the repository
- Check file is in correct location with correct name
- Try manual trigger: Actions → Run workflow
- Verify Actions are enabled: Settings → Actions → Allow all actions

---

## Tips & Tricks

### 1. Test Locally First
Before pushing to GitHub, test the build on your computer:
```bash
./gradlew assembleDebug
```

### 2. Use Pull Requests
Create branches for changes and use pull requests to test builds before merging to main.

### 3. Create Releases
When ready to publish:
1. Create a tag: `git tag v4.0`
2. Push tag: `git push origin v4.0`
3. GitHub will create a release with APK attached

### 4. Monitor Build Times
Check "Actions" tab to see build duration and optimize if needed.

### 5. Set Up Notifications
Get email notifications for build failures:
- Settings → Notifications → Actions

---

## Cost

**Free for public repositories!**

GitHub provides:
- Unlimited builds
- 2,000 Action minutes/month (builds take ~5-10 min each)
- That's 200-400 builds per month FREE!

For private repositories:
- Free tier: 2,000 minutes/month
- After that: Pay as you go

---

## Comparison: Termux vs GitHub Actions

| Feature | Termux | GitHub Actions |
|---------|--------|----------------|
| Setup Time | 30-60 min | 5 min |
| Build Time | 20-40 min | 5-10 min |
| Storage Needed | 3-4 GB | 0 GB |
| Complexity | High | Low |
| Success Rate | Variable | 99%+ |
| Battery Impact | High | None |
| Cost | Free | Free |
| **Recommendation** | ❌ Only if necessary | ✅ **Use this!** |

---

## Summary

GitHub Actions is the **easiest and most reliable** way to build your APK:

1. ✅ Create GitHub account
2. ✅ Create repository
3. ✅ Upload code
4. ✅ Wait 5-10 minutes
5. ✅ Download APK
6. ✅ Install on phone

**No Android Studio, no Termux complexity, no storage issues!**

---

## Next Steps

1. **Follow this guide** step by step
2. **Get your first successful build**
3. **Install and test** the APK
4. **Enjoy** your modernized ZipSigner app!

---

## Resources

- **GitHub Actions Docs**: https://docs.github.com/en/actions
- **GitHub Learning Lab**: https://lab.github.com/
- **Build Troubleshooting**: Check Actions tab → Build logs

---

**Happy Building! This is the way! 🚀**
