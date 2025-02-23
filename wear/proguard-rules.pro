# Keep all classes in the application package
-keep class com.turtlepaw.health.** { *; }

# Keep all classes in Android Wear Compose library
-keep class androidx.wear.compose.** { *; }

# Keep all classes in AndroidX Compose library
-keep class androidx.compose.** { *; }

# Keep broadcast receivers
-keep class com.turtlepaw.health.services.** { *; }
-keep class com.turtlepaw.health.services.SensorReceiver { *; }
-keep class com.turtlepaw.health.services.** { *; }
# Light
-keep class com.turtlepaw.health.services.LightLoggerService { *; }
-keep class com.turtlepaw.health.services.LightWorker { *; }
# Keep utils
-keep class com.turtlepaw.health.utils.** { *; }

-keep class com.turtlepaw.health.services.HealthWorkerKt { *; }
-keep class androidx.health.services.client.** { *; }

-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type

-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
# Keep all class, method, and field names
-keepnames class **
-keepclassmembers,allowshrinking class * { *; }

# Disable obfuscation (renaming)
-dontobfuscate