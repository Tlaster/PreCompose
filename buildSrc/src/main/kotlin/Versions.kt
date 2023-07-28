import org.gradle.api.JavaVersion

object Versions {
    const val precompose = "1.4.4"
    object Android {
        const val min = 21
        const val compile = 33
        const val target = compile
        const val buildTools = "34.0.0"
    }

    object Kotlin {
        const val lang = "1.9.0" // also check buildSrc/build.gradle.kts
        const val coroutines = "1.7.3"
    }

    object Java {
        const val jvmTarget = "17"
        val java = JavaVersion.VERSION_17
    }

    const val spotless = "6.7.0"
    const val ktlint = "0.45.2"
    const val compose = "1.5.0-beta03"
    const val compose_jb = "1.5.0-beta01"
    const val skiko = "0.7.72"

    object AndroidX {
        const val activity = "1.7.2"
        const val appcompat = "1.6.1"
        const val coreKtx = "1.10.1"
    }

    object Js {
        const val webpackCli = "5.1.4"
        const val node = "16.13.0"
    }
}
