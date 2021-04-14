pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}

rootProject.name = "PreCompose"

include(":precompose")
include(":sample:android")
include(":sample:desktop")
include(":sample:common")