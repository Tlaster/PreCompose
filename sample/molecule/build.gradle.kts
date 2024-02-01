import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.jetbrains.compose)
    id("com.android.application")
}

kotlin {
    applyDefaultHierarchyTemplate()
    listOf(
        iosSimulatorArm64(),
        iosArm64(),
        iosX64(),
    )
    androidTarget()
    macosX64 {
        binaries {
            executable {
                entryPoint = "moe.tlaster.precompose.molecule.sample.main"
                freeCompilerArgs += listOf(
                    "-linker-option", "-framework", "-linker-option", "Metal",
                )
            }
        }
    }
    macosArm64 {
        binaries {
            executable {
                entryPoint = "moe.tlaster.precompose.molecule.sample.main"
                freeCompilerArgs += listOf(
                    "-linker-option", "-framework", "-linker-option", "Metal",
                )
            }
        }
    }

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = rootProject.extra.get("jvmTarget") as String
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.ui)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(project(":precompose"))
                implementation(project(":precompose-molecule"))
                implementation(libs.molecule.runtime)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }
    }

    targets.withType<KotlinNativeTarget> {
        binaries.all {
            // TODO: the current compose binary surprises LLVM, so disable checks for now.
            freeCompilerArgs += "-Xdisable-phases=VerifyBitcode"
            binaryOptions["memoryModel"] = "experimental"
        }
    }
}

compose {
    desktop {
        application {
            mainClass = "moe.tlaster.precompose.molecule.sample.MainKt"
            nativeDistributions {
                targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                packageName = "moe.tlaster.precompose.sample.molecule"
                packageVersion = "1.0.0"
                macOS {
                    bundleID = "moe.tlaster.precompose.sample.molecule"
                    // iconFile.set(project.file("src/jvmMain/resources/icon/ic_launcher.icns"))
                }
                linux {
                    // iconFile.set(project.file("src/jvmMain/resources/icon/ic_launcher.png"))
                }
                windows {
                    shortcut = true
                    menu = true
                    // iconFile.set(project.file("src/jvmMain/resources/icon/ic_launcher.ico"))
                }
            }
        }
        nativeApplication {
            targets(kotlin.targets.getByName("macosX64"), kotlin.targets.getByName("macosArm64"))
            distributions {
                targetFormats(TargetFormat.Dmg)
                packageName = "moe.tlaster.precompose.sample.molecule"
                packageVersion = "1.0.0"
                macOS {
                    bundleID = "moe.tlaster.precompose.sample.molecule"
                    // iconFile.set(project.file("src/jvmMain/resources/icon/ic_launcher.icns"))
                }
            }
        }
    }
    // experimental {
    //     uikit {
    //         application {
    //             bundleIdPrefix = "moe.tlaster.precompose.sample.molecule"
    //             projectName = "PreComposeMoleculeSample"
    //             deployConfigurations {
    //                 simulator("Simulator") {
    //                     device = org.jetbrains.compose.experimental.dsl.IOSDevices.IPHONE_13_MINI
    //                 }
    //             }
    //         }
    //     }
    // }
}

android {
    compileSdk = rootProject.extra.get("android-compile") as Int
    buildToolsVersion = rootProject.extra.get("android-build-tools") as String
    namespace = "moe.tlaster.precompose.molecule.sample"
    defaultConfig {
        applicationId = "moe.tlaster.precompose.molecule.sample"
        minSdk = rootProject.extra.get("androidMinSdk") as Int
        targetSdk = rootProject.extra.get("androidTargetSdk") as Int
        versionCode = 1
        versionName = "0.1.0"
    }
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
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
}
