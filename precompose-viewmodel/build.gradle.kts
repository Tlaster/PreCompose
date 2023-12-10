import java.util.Properties

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.jetbrains.compose)
    id("com.android.library")
    id("maven-publish")
    id("signing")
}

group = "moe.tlaster"
version = rootProject.extra.get("precomposeVersion") as String

kotlin {
    applyDefaultHierarchyTemplate {
        common {
            group("jvmAndroid") {
                withAndroidTarget()
                withJvm()
            }
        }
    }
    androidTarget {
        publishLibraryVariants("release", "debug")
    }
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = rootProject.extra.get("jvmTarget") as String
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    macosArm64()
    macosX64()
    // ios()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    js(IR) {
        browser()
    }
    wasmJs {
        browser()
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                compileOnly(compose.foundation)
                implementation(project(":precompose"))
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
                api(libs.androidx.activity.ktx)
                implementation(libs.foundation)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation(libs.junit)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.foundation)
                api(libs.kotlinx.coroutines.swing)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation(libs.junit.jupiter.api)
                runtimeOnly(libs.junit.jupiter.engine)
            }
        }
        val macosMain by getting {
            dependencies {
                implementation(compose.foundation)
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(compose.foundation)
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
        val iosMain by getting {
            dependencies {
                implementation(compose.foundation)
            }
        }
    }
}

android {
    compileSdk = rootProject.extra.get("android-compile") as Int
    buildToolsVersion = rootProject.extra.get("android-build-tools") as String
    namespace = "moe.tlaster.precompose.viewmodel"
    defaultConfig {
        minSdk = rootProject.extra.get("androidMinSdk") as Int
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(rootProject.extra.get("jvmTarget") as String)
        targetCompatibility = JavaVersion.toVersion(rootProject.extra.get("jvmTarget") as String)
    }
}

extra.apply {
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
val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}
// https://github.com/gradle/gradle/issues/26091
val signingTasks = tasks.withType<Sign>()
tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(signingTasks)
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
            name.set("PreCompose-ViewModel")
            description.set("PreCompose ViewModel intergration")
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
