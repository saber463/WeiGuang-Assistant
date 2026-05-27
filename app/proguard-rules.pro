# 微光畅行 ProGuard 混淆规则
# 适用版本：2.0.0

# ---- 基本保留规则 ----
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---- Kotlin ----
-keep class kotlin.** { *; }
-keep class org.jetbrains.kotlin.** { *; }
-dontwarn kotlin.**
-dontwarn org.jetbrains.kotlin.**

# ---- 应用自身代码 ----
-keep class com.weiguangchangxing.weiguang_plus.** { *; }
-keep class com.weiguangchangxing.weiguang_plus.data.** { *; }
-keep class com.weiguangchangxing.weiguang_plus.core.** { *; }
-keep class com.weiguangchangxing.weiguang_plus.feature.** { *; }
-keep class com.weiguangchangxing.weiguang_plus.ui.** { *; }

# ---- Compose ----
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class * extends androidx.compose.runtime.State { *; }
-keep class * extends androidx.compose.runtime.MutableState { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-keepclassmembers class * {
    @androidx.compose.runtime.Immutable <fields>;
}
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep @androidx.compose.runtime.Composable class * { *; }
-keep @androidx.compose.runtime.Stable class * { *; }
-keep @androidx.compose.runtime.Immutable class * { *; }

# ---- CameraX ----
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# ---- ML Kit ----
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ---- Lottie ----
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# ---- Room ----
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**

# ---- DataStore ----
-dontwarn androidx.datastore.**

# ---- 协程 ----
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ---- GSON / JSON ----
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ---- 枚举 ----
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ---- 序列化 ----
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ---- MediaPipe ----
-keep class com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.**
-keep class com.google.mediapipe.tasks.** { *; }
-dontwarn com.google.mediapipe.tasks.**
-keep class com.google.mediapipe.framework.** { *; }
-dontwarn com.google.mediapipe.framework.**
-keep class com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**

# ---- AutoValue / JavaPoet (MediaPipe 依赖) ----
# 这些是 JDK 编译期类，运行时不需要，R8 会报 Missing class
-dontwarn autovalue.shaded.com.squareup.javapoet.**
-dontwarn com.google.auto.value.**
-dontwarn javax.lang.model.**
-dontwarn autovalue.shaded.**
-keep class autovalue.shaded.** { *; }

# ---- 反射 ----
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ---- OkHttp ----
-dontwarn okhttp3.**
-dontwarn okio.**

# ---- 资源压缩 ----
-keep class **.R$* { *; }