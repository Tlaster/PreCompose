plugins {
    kotlin("multiplatform")
    alias(libs.plugins.jetbrains.compose)
    id("com.android.library")
    alias(libs.plugins.compose.compiler)
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
                api(libs.koin.compose.viewmodel)
                api(project(":precompose"))
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
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "moe.tlaster.common"
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    kotlin.jvmToolchain(libs.versions.java.get().toInt())
}

compose.experimental {
    web.application {}
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

afterEvaluate {
    rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
        versions.webpackCli.version = libs.versions.webpackCliVersion.get()
        version = libs.versions.nodeVersion.get()
    }
}
