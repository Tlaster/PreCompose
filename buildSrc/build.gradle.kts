plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("com.android.tools.build:gradle:7.3.1")
    api(kotlin("gradle-plugin", version = "1.7.20"))
}