buildscript {
    repositories {
        gradlePluginPortal()
        jcenter()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.31")
        classpath("com.android.tools.build:gradle:4.0.2")
    }
}

plugins {
    id("com.diffplug.spotless").version("5.6.1")
}

allprojects {
    apply(plugin = "com.diffplug.spotless")
    repositories {
        jcenter()
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}

subprojects {
    spotless {
        kotlin {
            target("**/*.kt")
            targetExclude("$buildDir/**/*.kt")
            targetExclude("bin/**/*.kt")
            ktlint("0.41.0")
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint("0.41.0")
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            allWarningsAsErrors = true
            jvmTarget = "11"
        }
    }
}