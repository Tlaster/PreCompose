plugins {
    id("org.gradle.kotlin.kotlin-dsl") version "4.1.1"
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("com.android.tools.build:gradle:8.1.0")
    api(kotlin("gradle-plugin", version = "1.9.0"))
}