
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    ios("uikit") {
        binaries {
            executable {
                entryPoint = "main"
                freeCompilerArgs += listOf(
                    "-linker-option", "-framework", "-linker-option", "Metal",
                    "-linker-option", "-framework", "-linker-option", "CoreText",
                    "-linker-option", "-framework", "-linker-option", "CoreGraphics",
                )
                // TODO: the current compose binary surprises LLVM, so disable checks for now.
                freeCompilerArgs += "-Xdisable-phases=VerifyBitcode"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":sample:todo:common"))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

// compose.experimental {
//     uikit.application {
//         bundleIdPrefix = "moe.tlaster"
//         projectName = "PreComposeSample"
//         deployConfigurations {
//             simulator("Simulator") {
//                 device = org.jetbrains.compose.experimental.dsl.IOSDevices.IPHONE_13_MINI
//             }
//         }
//     }
// }

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = rootProject.extra.get("jvmTarget") as String
}

kotlin {
    targets.withType<KotlinNativeTarget> {
        binaries.all {
            // TODO: the current compose binary surprises LLVM, so disable checks for now.
            freeCompilerArgs += "-Xdisable-phases=VerifyBitcode"
            binaryOptions["memoryModel"] = "experimental"
        }
    }
}
