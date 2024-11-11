@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.turtlepaw.health"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.turtlepaw.health"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11" // Replace with your Compose version
    }
}

dependencies {
    // Room
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.coroutines.play.services)

    implementation("io.github.raamcosta.compose-destinations:core:2.1.0-beta14")
    ksp("io.github.raamcosta.compose-destinations:ksp:2.1.0-beta14")

    // for bottom sheet destination support, also add
    implementation("io.github.raamcosta.compose-destinations:bottom-sheet:2.1.0-beta14")

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
    implementation(platform("androidx.compose:compose-bom:2024.09.02"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.02"))

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
}

ksp {
    arg("compose-destinations.generateNavGraphs", "true") // Enable NavGraphs generation
    arg("compose-destinations.navGraph.visibility", "internal") // Set visibility (optional)
    arg("compose-destinations.navGraph.moduleName", "app") // Set module name (optional)
}