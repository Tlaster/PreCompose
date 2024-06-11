plugins {
    alias(libs.plugins.jetbrains.compose)
    id("com.android.application")
    kotlin("android")
    alias(libs.plugins.compose.compiler)
}

group = "moe.tlaster"
version = "1.0"

dependencies {
    implementation(project(":sample:todo:common"))
    implementation(libs.androidx.activity.compose)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        applicationId = "moe.tlaster.android"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.compileSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.java.get())
    }
    namespace = "moe.tlaster.android"
}
