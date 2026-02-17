# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep all logging classes
-keep class kellinwood.logging.** { *; }

# Keep BouncyCastle security provider classes
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
-keepnames class org.bouncycastle.** { *; }

# Keep zip signing classes
-keep class kellinwood.security.zipsigner.** { *; }
-keep class kellinwood.zipio.** { *; }

# Keep Android security classes
-keep class javax.security.** { *; }
-keep class java.security.** { *; }

# Suppress warnings for missing classes
-dontwarn java.awt.**
-dontwarn javax.naming.**
-dontwarn sun.security.**

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom view constructors
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep Activity classes
-keep public class * extends androidx.appcompat.app.AppCompatActivity
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
