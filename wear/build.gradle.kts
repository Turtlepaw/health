//@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    id("com.google.devtools.ksp")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.turtlepaw.health"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.turtlepaw.health"
        minSdk = 30
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("debugging") {
            storeFile = file("../debug.keystore")
            keyAlias = "androiddebugkey"
            storePassword = "android"
            keyPassword = "android"
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debugging")
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.play.services.wearable)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.tiles)
    implementation(libs.androidx.tiles.material)
    implementation(libs.androidx.watchface.complications.data.source.ktx)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.play.services.pal)
    implementation(libs.androidx.wear.tooling.preview)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.wear)
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.core)
    implementation(libs.compose.shimmer)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.health.services.client)
    implementation(libs.androidx.runtime.livedata)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Horologist
    implementation(libs.horologist.health.service)
    implementation(libs.horologist.health.composables)
    implementation(libs.horologist.compose.material)
    implementation(libs.horologist.compose.layout)
    implementation(libs.horologist.composables)
    implementation(libs.horologist.compose.tools)
    implementation(libs.horologist.tiles)

    // Icons
    implementation(libs.material.icons.extended)
    debugImplementation(libs.androidx.tiles.tooling)

    // Room
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Gson
    implementation(libs.gson)

    // Wear OS
    implementation(libs.androidx.wear.ongoing)
    implementation(libs.horologist.health.service)

    // Health services
    implementation(libs.androidx.health.services.client)

    // Bluetooth
    implementation(project(":heart_connection"))
    implementation(libs.jasonchenlijian.fastble)

    // Wear Tiles
    implementation(libs.androidx.tiles.tooling.preview)

    // Shared
    implementation(project(":shared"))

    // Animations
    implementation(libs.lottie.compose)

    // Maps
    implementation(libs.mapsforge.map.android)
    implementation(libs.androidsvg)
    implementation(libs.mapsforge.themes)

    implementation("com.google.maps.android:maps-compose:6.4.0")

    // Optionally, you can include the Compose utils library for Clustering,
    // Street View metadata checks, etc.
    implementation("com.google.maps.android:maps-compose-utils:6.4.0")

    // Media
    implementation(project(":live_media"))

    //wearApp(project(":wear"))
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}