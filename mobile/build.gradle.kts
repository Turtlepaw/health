@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
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
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Room
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.coroutines.play.services)

    implementation(libs.core)
    ksp(libs.ksp)

    // for bottom sheet destination support, also add
    implementation(libs.bottom.sheet)

    // AndroidX and core libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.play.services.wearable)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Compose BOM and related libraries
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Jetpack Compose dependencies
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.foundation:foundation-layout")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.ui:ui-tooling")
    implementation(libs.androidx.activity.compose)
    implementation(project(":shared"))

    // Icons
    implementation(libs.material.icons.extended)

    implementation(libs.androidx.lifecycle.viewmodel.android)

    implementation(libs.accompanist.systemuicontroller)

    implementation("androidx.compose.runtime:runtime:1.7.5") // Replace with the latest version
    implementation("androidx.compose.runtime:runtime-livedata:1.7.5")

    // Gson
    implementation(libs.gson)

    // Work
    implementation(libs.androidx.work.runtime.ktx)
}

ksp {
    arg("compose-destinations.generateNavGraphs", "true") // Enable NavGraphs generation
    arg("compose-destinations.navGraph.visibility", "internal") // Set visibility (optional)
    arg("compose-destinations.navGraph.moduleName", "mobile") // Set module name (optional)
}