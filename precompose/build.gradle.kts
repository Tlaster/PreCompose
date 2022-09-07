import org.jetbrains.compose.compose
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
    ios("uikit")
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
                api(compose.foundation)
                api(compose.animation)
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.Kotlin.coroutines}")
                implementation("com.benasher44:uuid:0.4.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                // implementation(compose("org.jetbrains.compose.ui:ui-test-junit4"))
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.activity:activity-ktx:${Versions.AndroidX.activity}")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
        }
        val macosMain by creating {
            dependsOn(commonMain)
        }
        val macosX64Main by getting {
            dependsOn(macosMain)
        }
        val macosArm64Main by getting {
            dependsOn(macosMain)
        }
        val jvmMain by getting {
            dependencies {
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
        }
    }
}

android {
    compileSdk = Versions.Android.compile
    buildToolsVersion = Versions.Android.buildTools
    namespace = "moe.tlaster.precompose"
    defaultConfig {
        minSdk = Versions.Android.min
        targetSdk = Versions.Android.target
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
            scm {
                url.set("https://github.com/Tlaster/PreCompose")
                connection.set("scm:git:git://github.com/Tlaster/PreCompose.git")
                developerConnection.set("scm:git:git://github.com/Tlaster/PreCompose.git")
            }
        }
    }
}
