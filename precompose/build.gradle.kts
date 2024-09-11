import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.android.library)
    id("maven-publish")
    id("signing")
    alias(libs.plugins.compose.compiler)
}

group = "moe.tlaster"
version = libs.versions.libVersion.get()

kotlin {
    applyDefaultHierarchyTemplate()
    macosArm64()
    macosX64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    androidTarget {
        publishLibraryVariants("release", "debug")

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant {
            sourceSetTree.set(KotlinSourceSetTree.test)

            dependencies {
                androidTestImplementation("androidx.compose.ui:ui-test-junit4-android:1.6.8")
                debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.1")
            }
        }
    }
    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
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
                compileOnly(compose.animation)
                compileOnly(compose.material)
                api(libs.kotlinx.coroutines.core)
                implementation(libs.uuid)
            }
        }
        val commonTest by getting {
            dependencies {
                api(compose.foundation)
                api(compose.animation)
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(libs.kotlinx.coroutines.test)
                @OptIn(ExperimentalComposeLibrary::class)
                implementation(compose.uiTest)
                // @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                // implementation(compose.uiTestJUnit4)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.foundation)
                implementation(libs.animation)
                implementation(libs.androidx.material)
                api(libs.androidx.activity.ktx)
                implementation(libs.androidx.lifecycle.runtime.ktx)
                api(libs.androidx.savedstate.ktx)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.activity.compose)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation(libs.junit)
            }
        }
        val macosMain by getting {
            dependencies {
                implementation(compose.foundation)
                implementation(compose.animation)
                implementation(compose.material)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.foundation)
                implementation(compose.animation)
                implementation(compose.material)
                api(libs.kotlinx.coroutines.swing)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation(libs.junit.jupiter.api)
                runtimeOnly(libs.junit.jupiter.engine)
                implementation(compose.desktop.currentOs)
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(compose.foundation)
                implementation(compose.animation)
                implementation(compose.material)
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
                implementation(compose.animation)
                implementation(compose.material)
            }
        }
        val wasmJsMain by getting {
            dependencies {
                implementation(compose.foundation)
                implementation(compose.animation)
                implementation(compose.material)
            }
        }
    }
}
// adding it here to make sure skiko is unpacked and available in web tests
compose.experimental {
    web.application {}
}
android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "moe.tlaster.precompose"
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.java.get())
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
                val releasesRepoUrl =
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                val snapshotsRepoUrl =
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
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
