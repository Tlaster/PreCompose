plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.kotlin.android)
}

android {
  compileSdk = rootProject.extra.get("android-compile") as Int
  buildToolsVersion = rootProject.extra.get("android-build-tools") as String
  defaultConfig {
    applicationId = "moe.tlaster.sample.shared.elements"
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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  namespace = "moe.tlaster.sample.shared.elements"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_17.toString()
    allWarningsAsErrors = true
    freeCompilerArgs = listOf(
      "-opt-in=kotlin.RequiresOptIn",
    )
  }
}

//noinspection UseTomlInstead
dependencies {
  implementation("androidx.compose.animation:animation-core:1.7.0-SNAPSHOT")
  implementation("androidx.compose.ui:ui:1.7.0-SNAPSHOT")
  implementation("androidx.compose.foundation:foundation:1.7.0-SNAPSHOT")
  implementation(libs.androidx.activity.compose)
  implementation(project(":precompose"))
}
