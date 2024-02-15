plugins {
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.spotless)
}

allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = rootProject.extra.get("jvmTarget") as String
            allWarningsAsErrors = true
            freeCompilerArgs = listOf(
                "-opt-in=kotlin.RequiresOptIn",
            )
        }
    }
    apply(plugin = "com.diffplug.spotless")
    spotless {
        kotlin {
            target("**/*.kt")
            targetExclude("$buildDir/**/*.kt", "bin/**/*.kt", "buildSrc/**/*.kt")
            ktlint(rootProject.extra.get("ktlintVersion") as String)
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint(rootProject.extra.get("ktlintVersion") as String)
        }
        java {
            target("**/*.java")
            targetExclude("$buildDir/**/*.java", "bin/**/*.java")
        }
    }
}

extra.apply {
    set("precomposeVersion", "1.6.0-rc01")

    set("jvmTarget", "11")

    // Android configurations
    set("android-compile", 34)
    set("android-build-tools", "34.0.0")
    set("androidMinSdk", 21)
    set("androidTargetSdk", 34)

    // Js & Node
    set("webpackCliVersion", "5.1.4")
    set("nodeVersion", "16.13.0")

    set("ktlintVersion", "0.50.0")
}
