plugins {
    alias(libs.plugins.jetbrains.compose)
    id("com.android.application")
    kotlin("android")
}

group = "moe.tlaster"
version = "1.0"

dependencies {
    implementation(project(":sample:todo:common"))
    implementation(libs.androidx.activity.compose)
}

android {
    compileSdk = rootProject.extra.get("android-compile") as Int
    buildToolsVersion = rootProject.extra.get("android-build-tools") as String
    defaultConfig {
        applicationId = "moe.tlaster.android"
        minSdk = rootProject.extra.get("androidMinSdk") as Int
        targetSdk = rootProject.extra.get("androidTargetSdk") as Int
        versionCode = 1
        versionName = "0.1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(rootProject.extra.get("jvmTarget") as String)
        targetCompatibility = JavaVersion.toVersion(rootProject.extra.get("jvmTarget") as String)
    }
    namespace = "moe.tlaster.android"
}
