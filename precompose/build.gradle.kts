
import java.util.Properties

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version Versions.compose_jb
    id("com.android.library")
    id("maven-publish")
    id("signing")
}

group = "moe.tlaster"
version = Versions.precompose

kotlin {
    macosArm64()
    macosX64()
    // ios("uikit")
    iosX64("uikitX64")
    iosArm64("uikitArm64")
    iosSimulatorArm64("uikitSimulatorArm64")
    android {
        publishLibraryVariants("release", "debug")
    }
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = Versions.Java.jvmTarget
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        browser()
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                compileOnly(compose.foundation)
                compileOnly(compose.animation)
                compileOnly(compose.material)
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.Kotlin.coroutines}")
            }
        }
        val commonTest by getting {
            dependencies {
                api(compose.foundation)
                api(compose.animation)
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                // @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                // implementation(compose.uiTestJUnit4)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.compose.foundation:foundation:${Versions.compose}")
                implementation("androidx.compose.animation:animation:${Versions.compose}")
                implementation("androidx.compose.material:material:${Versions.compose}")
                api("androidx.activity:activity-ktx:${Versions.AndroidX.activity}")
                implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
                api("androidx.savedstate:savedstate-ktx:1.2.1")
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
        }
        val macosMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(compose.foundation)
                implementation(compose.animation)
                implementation(compose.material)
            }
        }
        val macosX64Main by getting {
            dependsOn(macosMain)
        }
        val macosArm64Main by getting {
            dependsOn(macosMain)
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.foundation)
                implementation(compose.animation)
                implementation(compose.material)
                api("org.jetbrains.kotlinx:kotlinx-coroutines-swing:${Versions.Kotlin.coroutines}")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation("org.junit.jupiter:junit-jupiter-api:5.8.0")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.0")
            }
        }
        val jsMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(compose.foundation)
                implementation(compose.animation)
                implementation(compose.material)
            }
        }
        val uikitMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(compose.foundation)
                implementation(compose.animation)
                implementation(compose.material)
            }
        }
        val uikitX64Main by getting {
            dependsOn(uikitMain)
        }
        val uikitArm64Main by getting {
            dependsOn(uikitMain)
        }
        val uikitSimulatorArm64Main by getting {
            dependsOn(uikitMain)
        }
    }
}

android {
    compileSdk = Versions.Android.compile
    buildToolsVersion = Versions.Android.buildTools
    namespace = "moe.tlaster.precompose"
    defaultConfig {
        minSdk = Versions.Android.min
    }
    compileOptions {
        sourceCompatibility = Versions.Java.java
        targetCompatibility = Versions.Java.java
    }
}

ext {
    val publishPropFile = rootProject.file("publish.properties")
    if (publishPropFile.exists()) {
        Properties().apply {
            load(publishPropFile.inputStream())
        }.forEach { name, value ->
            if (name == "signing.secretKeyRingFile") {
                set(name.toString(), rootProject.file(value.toString()).absolutePath)
            } else {
                set(name.toString(), value)
            }
        }
    } else {
        set("signing.keyId", System.getenv("SIGNING_KEY_ID"))
        set("signing.password", System.getenv("SIGNING_PASSWORD"))
        set("signing.secretKeyRingFile", System.getenv("SIGNING_SECRET_KEY_RING_FILE"))
        set("ossrhUsername", System.getenv("OSSRH_USERNAME"))
        set("ossrhPassword", System.getenv("OSSRH_PASSWORD"))
    }
}

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    if (rootProject.file("publish.properties").exists()) {
        signing {
            sign(publishing.publications)
        }
        repositories {
            maven {
                val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                url = if (version.toString().endsWith("SNAPSHOT")) {
                    uri(snapshotsRepoUrl)
                } else {
                    uri(releasesRepoUrl)
                }
                credentials {
                    username = project.ext.get("ossrhUsername").toString()
                    password = project.ext.get("ossrhPassword").toString()
                }
            }
        }
    }

    publications.withType<MavenPublication> {
        artifact(javadocJar)
        pom {
            name.set("PreCompose")
            description.set("A third-party Jetbrains Compose library with ViewModel, LiveData and Navigation support.")
            url.set("https://github.com/Tlaster/PreCompose")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("Tlaster")
                    name.set("James Tlaster")
                    email.set("tlaster@outlook.com")
                }
            }
            scm {
                url.set("https://github.com/Tlaster/PreCompose")
                connection.set("scm:git:git://github.com/Tlaster/PreCompose.git")
                developerConnection.set("scm:git:git://github.com/Tlaster/PreCompose.git")
            }
        }
    }
}
