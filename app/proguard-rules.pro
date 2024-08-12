# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#-keep public class com.konovalov.vad.silero.** { *; }
#-keep class app.amazeai.android.components.SpeechToTextManager { *; }
#keep play-services-auth

#-dontwarn com.google.type.**
#-dontwarn com.google.rpc.**
#-dontwarn java.beans.ConstructorProperties
#-keepnames class java.beans.** { *; }
#-keepnames class io.ktor.** { *; }
-dontwarn  java.beans.**

-keep class com.google.firebase.perf.** { *; }
-keep class com.google.firebase.perf.v1.TraceMetric { *; }
-keep class com.google.firebase.perf.v1.ApplicationProcessState { *; }
-keep class com.google.firebase.perf.v1.ApplicationInfo { *; }

-keep class google.cloud.speech.** { *; }
-keep class com.google.firestore.** { *; }
-keep class com.google.type.** { *; }
-keep class com.google.rpc.** { *; }
-keep class  java.beans.** { *; }
-keep class  com.fasterxml.** { *; }
-keep class  com.google.api.** { *; }

-keep class com.google.protobuf.** { *; }
-keepnames class com.google.protobuf.** { *; }
-keepnames class com.google.type.** { *; }
-keepnames class com.google.rpc.** { *; }
-keepnames class com.google.firestore.** { *; }
-keepnames class com.google.firestore.v1.** { *; }


-keep class androidx.biometric.** { *; }
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
  *;
}
-keep class org.apache.http.** { *; }
-keep class org.apache.httpcomponents.** { *; }
-keep class ai.onnxruntime.** { *; }
-keep class com.backblaze.** { *; }
-keep class app.amazeai.android.data.model.** { *; }
-keep public class com.itextpdf.**
# Firebase
-keep class com.firebase.** { *; }
-keep class org.apache.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.apache.**
-dontwarn org.w3c.dom.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-dontwarn sun.security.util.ObjectIdentifier
-dontwarn sun.security.x509.AlgorithmId
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.xml.sax.driver

# Keep the annotations that proguard needs to process.
-keep class com.google.android.filament.proguard.UsedBy*

# Just because native code accesses members of a class, does not mean that the
# class itself needs to be annotated - only annotate classes that are
# referenced themselves in native code.
-keep @com.google.android.filament.proguard.UsedBy* class * {
  <init>();
}
-keepclassmembers class * {
  @com.google.android.filament.proguard.UsedBy* *;
}