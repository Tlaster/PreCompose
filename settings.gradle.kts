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
    }
}
rootProject.name = "precompose"

include(":precompose")
include(":precompose-molecule")
include(":sample:todo:android")
include(":sample:todo:desktop")
include(":sample:todo:common")
include(":sample:todo:ios")
include(":sample:todo:macos")
include(":sample:todo:js")
//include(":sample:molecule")
