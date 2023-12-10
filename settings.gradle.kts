pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    // https://youtrack.jetbrains.com/issue/KT-51379
    // repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.mozilla.org/maven2/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://jitpack.io")
        // TODO: delete when we have all libs in mavenCentral
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    }
}
rootProject.name = "precompose"

include(":precompose")
include(":precompose-viewmodel")
include(":precompose-molecule")
include(":precompose-koin")
include(":sample:todo:android")
include(":sample:todo:desktop")
include(":sample:todo:common")
include(":sample:todo:ios")
include(":sample:todo:macos")
include(":sample:todo:js")
include(":sample:molecule")
