plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("com.android.tools.build:gradle:8.1.0")
    api(kotlin("gradle-plugin", version = "1.8.20"))
}