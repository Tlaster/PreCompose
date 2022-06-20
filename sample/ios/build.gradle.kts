import org.jetbrains.compose.compose
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform") id("org.jetbrains.compose") version Versions.compose_jb
}

kotlin {
    ios("uikit") {
        binaries {
            executable {
                entryPoint = "main"
                freeCompilerArgs += listOf(
                    "-linker-option", "-framework", "-linker-option", "Metal",
                    "-linker-option", "-framework", "-linker-option", "CoreText",
                    "-linker-option", "-framework", "-linker-option", "CoreGraphics"
                )
                // TODO: the current compose binary surprises LLVM, so disable checks for now.
                freeCompilerArgs += "-Xdisable-phases=VerifyBitcode"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":sample:common"))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

compose.experimental {
    uikit.application {
        bundleIdPrefix = "moe.tlaster"
        projectName = "PreComposeSample"
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.Java.jvmTarget
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
