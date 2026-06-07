# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /sdk/tools/proguard/proguard-android-optimize.txt

# Keep data classes
-keep class com.parking.billing.FeeBreakdown { *; }

# Keep Kotlin metadata
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep Compose
-keep class androidx.compose.** { *; }
