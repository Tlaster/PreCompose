plugins {
    id("org.jetbrains.compose") version "0.4.0-build211"
    id("com.android.application")
    kotlin("android")
}

group = "moe.tlaster"
version = "1.0"

repositories {
    google()
}

dependencies {
    implementation(project(":sample:common"))
}

android {
    compileSdkVersion(30)
    defaultConfig {
        applicationId = "moe.tlaster.android"
        minSdkVersion(21)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "0.1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    lintOptions {
        isCheckReleaseBuilds = false
    }
}
