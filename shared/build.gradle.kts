import org.gradle.kotlin.dsl.ksp

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.turtlepaw.shared"
    compileSdk = 34

    defaultConfig {
        minSdk = 30

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.lifecycle.viewmodel.android)

    // Room
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Gson
    implementation(libs.gson)

    // Health
    implementation(project(":heart_connection"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}