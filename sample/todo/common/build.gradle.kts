plugins {
    kotlin("multiplatform")
    alias(libs.plugins.jetbrains.compose)
    id("com.android.library")
}

kotlin {
    applyDefaultHierarchyTemplate()
    macosX64()
    macosArm64()
    iosArm64()
    iosX64()
    iosSimulatorArm64()
    androidTarget()
    jvm("desktop")
    js(IR) {
        browser()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(libs.koin)
                api(libs.koin.compose)
                api(project(":precompose"))
                api(project(":precompose-viewmodel"))
                api(project(":precompose-koin"))
            }
        }
        val commonTest by getting
        val androidMain by getting {
            dependencies {
                api(libs.androidx.appcompat)
                api(libs.androidx.coreKtx)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
            }
        }
        val desktopMain by getting
        val desktopTest by getting
        // val jsMain by getting
    }
}

android {
    compileSdk = rootProject.extra.get("android-compile") as Int
    buildToolsVersion = rootProject.extra.get("android-build-tools") as String
    namespace = "moe.tlaster.common"
    defaultConfig {
        minSdk = rootProject.extra.get("androidMinSdk") as Int
    }
    kotlin.jvmToolchain((rootProject.extra.get("jvmTarget") as String).toInt())
}

compose.experimental {
    web.application {}
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

afterEvaluate {
    rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
        versions.webpackCli.version = rootProject.extra.get("webpackCliVersion") as String
        nodeVersion = rootProject.extra.get("nodeVersion") as String
    }
}
