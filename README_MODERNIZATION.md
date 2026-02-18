# ZipSigner - Updated for Modern Android

## Version 4.0 - Modernization Update

This is an updated version of the ZipSigner Android application, modernized for compatibility with current Android versions (Android 15 / API 35).

---

## Major Updates Applied

### 1. **Build System Modernization**

#### Gradle Updates
- **Gradle**: Updated from 5.4.1 to 8.9
- **Android Gradle Plugin**: Updated from 3.5.2 to 8.7.3
- **Build Tools**: Latest stable versions

#### Repository Changes
- **Removed**: `jcenter()` (deprecated and shut down)
- **Added**: `mavenCentral()` as the primary repository
- **Kept**: `google()` and `mavenLocal()`

### 2. **Android SDK & API Levels**

| Setting | Old Version | New Version |
|---------|-------------|-------------|
| compileSdk | 28 (Android 9) | 35 (Android 15) |
| targetSdk | 28 (Android 9) | 35 (Android 15) |
| minSdk | 14 (Android 4.0) | 24 (Android 7.0) |
| Java Version | 1.8 | 17 |

### 3. **AndroidX Migration**

The project has been fully migrated from old Android Support Libraries to AndroidX:

#### Library Updates
- `android.support.v7.app.AppCompatActivity` → `androidx.appcompat.app.AppCompatActivity`
- `com.android.support:appcompat-v7:28.0.0` → `androidx.appcompat:appcompat:1.7.0`

#### New AndroidX Dependencies
- `androidx.core:core-ktx:1.15.0`
- `androidx.constraintlayout:constraintlayout:2.2.0`
- `androidx.activity:activity:1.9.3`
- `androidx.lifecycle:lifecycle-runtime-ktx:2.8.7`
- `com.google.android.material:material:1.12.0`

### 4. **Security Library Updates**

#### BouncyCastle
- **Old**: `bcprov-jdk16:1.46` (2010)
- **New**: `bcprov-jdk18on:1.78.1` (2024)
- **Added**: `bcpkix-jdk18on:1.78.1` for enhanced PKI support

#### Removed
- `com.madgag:scpkix-jdk15on:1.47.0.2` (deprecated, functionality now in modern BouncyCastle)

### 5. **Modern Android Permissions**

#### Storage Access Modernization
The app now properly handles storage permissions across all Android versions:

**Android 6-10 (API 23-29):**
- `READ_EXTERNAL_STORAGE`
- `WRITE_EXTERNAL_STORAGE`

**Android 11-12 (API 30-32):**
- `MANAGE_EXTERNAL_STORAGE` (for full file system access)
- Legacy storage support with `requestLegacyExternalStorage`

**Android 13+ (API 33+):**
- `READ_MEDIA_IMAGES`
- `READ_MEDIA_VIDEO`
- `READ_MEDIA_AUDIO`
- `MANAGE_EXTERNAL_STORAGE`

#### New Permission Helper
Added `StoragePermissionHelper.java` class that intelligently handles permissions based on Android version.

### 6. **Manifest Updates**

#### Key Changes
- Removed `package` attribute (now defined in `build.gradle` as `namespace`)
- Added `android:exported` attributes to all Activities (required for Android 12+)
- Updated permission declarations with proper SDK version constraints
- Added `tools:targetApi` for backwards compatibility
- Enabled `allowBackup` and `supportsRtl`

### 7. **Build Configuration Enhancements**

#### New Features Enabled
- **View Binding**: Enabled for type-safe view access
- **Build Config**: Enabled for version constants
- **Vector Drawables**: Support library enabled

#### Build Optimizations
- Gradle caching enabled
- Parallel builds enabled
- Non-transitive R class enabled (reduces build size)

#### ProGuard/R8 Updates
Enhanced ProGuard rules for:
- BouncyCastle security providers
- Zip signing libraries
- Logging frameworks
- AndroidX libraries

### 8. **Code Modernization**

#### Activity Updates
All activities migrated from `Activity` to `AppCompatActivity`:
- `ZipPickerActivity`
- `ZipSignerActivity`
- `ManageKeysActivity`
- `CreateKeystoreFormActivity`
- `CreateKeyFormActivity`
- `CreateCertFormActivity`
- `KeysPropertiesActivity`
- `ManageKeysHelpActivity`
- `CreateKeystoreIntroActivity`

---

## Building the Project

### Prerequisites
- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: Version 17 or later
- **Android SDK**: API 35 (Android 15)

### Build Steps

1. **Open in Android Studio**
   ```bash
   File -> Open -> Select zip-signer-updated folder
   ```

2. **Sync Gradle**
   Android Studio will automatically sync Gradle files. If not:
   ```bash
   File -> Sync Project with Gradle Files
   ```

3. **Build APK**
   ```bash
   Build -> Build Bundle(s) / APK(s) -> Build APK(s)
   ```

4. **Build from Command Line**
   ```bash
   ./gradlew assembleDebug
   # or for release
   ./gradlew assembleRelease
   ```

### Output Locations
- **Debug APK**: `ZipSigner/build/outputs/apk/debug/`
- **Release APK**: `ZipSigner/build/outputs/apk/release/`

---

## Testing Checklist

### Functional Testing
- [ ] App launches successfully
- [ ] File picker works on all Android versions
- [ ] Zip signing functionality works
- [ ] Custom key creation and management
- [ ] Storage permissions granted properly
- [ ] App works on Android 7.0 (API 24)
- [ ] App works on Android 15 (API 35)

### Permission Testing
- [ ] Test on Android 7-9 (legacy permissions)
- [ ] Test on Android 10 (scoped storage transition)
- [ ] Test on Android 11+ (MANAGE_EXTERNAL_STORAGE)
- [ ] Test on Android 13+ (granular media permissions)

---

## Known Considerations

### Storage Access Framework
For optimal Android 11+ compatibility, consider migrating from direct file access to Storage Access Framework (SAF). The current implementation uses `MANAGE_EXTERNAL_STORAGE` which requires special Play Store approval.

### ListActivity Migration
`AndroidFileBrowser` still extends `ListActivity`. For future updates, consider migrating to:
- RecyclerView with AppCompatActivity
- Or use Android's built-in file picker (Storage Access Framework)

### Testing Recommendations
- Test thoroughly on physical devices running Android 11+ for storage access
- Verify signature verification on latest Android versions
- Test with APKs signed using different signature schemes (v1, v2, v3, v4)

---

## Migration from Original Version

### For Users
The app maintains backwards compatibility with existing keystores and configurations. Simply install the updated version.

### For Developers
If you've modified the original ZipSigner:

1. **Dependency Updates**: Check `ZipSigner/build.gradle` for new dependency versions
2. **Import Updates**: Update `import android.app.Activity` to `import androidx.appcompat.app.AppCompatActivity`
3. **Manifest**: Add `android:exported` to all Activity components
4. **Permissions**: Implement `StoragePermissionHelper` for runtime permissions
5. **ProGuard**: Update rules for BouncyCastle and AndroidX

---

## Version Information

| Component | Version |
|-----------|---------|
| App Version | 4.0 |
| Version Code | 40 |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 (Android 15) |
| Compile SDK | 35 (Android 15) |

---

## License

Original license (Apache 2.0) maintained. See source file headers for details.

## Credits

- **Original Author**: Ken Ellinwood
- **Modernization Update**: 2026
- **Repository**: https://github.com/kellinwood/zip-signer

---

## Additional Resources

- [Android Developer Guide - Storage](https://developer.android.com/training/data-storage)
- [AndroidX Migration Guide](https://developer.android.com/jetpack/androidx/migrate)
- [BouncyCastle Documentation](https://www.bouncycastle.org/java.html)
- [Android Code Signing](https://source.android.com/docs/security/features/apksigning)
