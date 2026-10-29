# ProGuard & R8 Configuration for FuelSync (Fuel Mileage Tracker)
# Optimization, shrinking, and obfuscation rules for release builds.

# ====================================================================
# General Android & Code Quality
# ====================================================================
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve Parcelable CREATOR fields
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# Preserve Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ====================================================================
# Jetpack Compose Rules
# ====================================================================
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
    @androidx.compose.runtime.ReadOnlyComposable *;
}

# Compose inspection & tooling preview support
-dontwarn androidx.compose.ui.tooling.**

# ====================================================================
# Kotlin Coroutines & Flow
# ====================================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ====================================================================
# AndroidX Room Database & SQLite
# ====================================================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep class **_Impl { *; }
-dontwarn androidx.room.paging.**

# Keep FuelSync entity and domain models
-keep class com.example.data.database.** { *; }
-keep class com.example.domain.model.** { *; }

# ====================================================================
# Moshi & Retrofit
# ====================================================================
-keepclasseswithmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep class * extends com.squareup.moshi.JsonAdapter { *; }
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-dontwarn com.squareup.moshi.**

-keep class retrofit2.** { *; }
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**

# ====================================================================
# AndroidX DataStore & Preferences
# ====================================================================
-keep class androidx.datastore.preferences.** { *; }
-dontwarn androidx.datastore.**

# ====================================================================
# Google Play Services & Firebase (Graceful handling)
# ====================================================================
-dontwarn com.google.android.gms.**
-dontwarn com.google.firebase.**
