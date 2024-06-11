import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.spotless)
    alias(libs.plugins.compose.compiler) apply false
}

allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.java.get()))
            allWarningsAsErrors.set(true)
            freeCompilerArgs.set(
                listOf(
                    "-Xcontext-receivers",
                    "-Xexpect-actual-classes",
                ),
            )
        }
    }
    apply(plugin = "com.diffplug.spotless")
    spotless {
        kotlin {
            target("**/*.kt")
            targetExclude("${layout.buildDirectory}/**/*.kt", "bin/**/*.kt", "buildSrc/**/*.kt")
            ktlint(libs.versions.ktlint.get())
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint(libs.versions.ktlint.get())
        }
        java {
            target("**/*.java")
            targetExclude("${layout.buildDirectory}/**/*.java", "bin/**/*.java")
        }
    }
}
