import org.gradle.api.JavaVersion

object Versions {
    const val precompose = "1.4.4"
    object Android {
        const val min = 21
        const val compile = 33
        const val target = compile
        const val buildTools = "33.0.0"
    }

    object Kotlin {
        const val lang = "1.9.0"
        const val coroutines = "1.7.3"
    }

    object Java {
        const val jvmTarget = "11"
        val java = JavaVersion.VERSION_11
    }

    const val spotless = "6.7.0"
    const val ktlint = "0.45.2"
    const val compose = "1.5.0-beta03"
    const val compose_jb = "1.5.0-dev1122"

    object AndroidX {
        const val activity = "1.7.2"
        const val appcompat = "1.6.1"
    }
}
