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