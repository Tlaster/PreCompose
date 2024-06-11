plugins {
    kotlin("multiplatform")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

val resourcesDir = "$buildDir/resources/"

val skikoWasm by configurations.creating

dependencies {
    skikoWasm(libs.skiko.js)
}

val unzipTask = tasks.register("unzipWasm", Copy::class) {
    destinationDir = file(resourcesDir)
    from(skikoWasm.map { zipTree(it) })
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile>().configureEach {
    dependsOn(unzipTask)
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":sample:todo:common"))
                implementation(compose.ui)
                implementation(compose.html.core)
                implementation(libs.skiko)
            }

            resources.setSrcDirs(resources.srcDirs)
            resources.srcDirs(unzipTask.map { it.destinationDir })
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jsMain by getting {
            dependsOn(commonMain)
        }
    }
}

compose.experimental {
    web.application {}
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

afterEvaluate {
    rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
        versions.webpackCli.version = libs.versions.webpackCliVersion.get()
        nodeVersion = libs.versions.nodeVersion.get()
    }
}
