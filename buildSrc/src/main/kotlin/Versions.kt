import org.gradle.api.JavaVersion

object Versions {
    const val precompose = "1.4.0"
    object Android {
        const val min = 21
        const val compile = 33
        const val target = compile
        const val buildTools = "33.0.0"
    }

    object Kotlin {
        const val lang = "1.8.00"
        const val coroutines = "1.6.4"
    }

    object Java {
        const val jvmTarget = "11"
        val java = JavaVersion.VERSION_11
    }

    const val spotless = "6.7.0"
    const val ktlint = "0.45.2"
    const val compose = "1.3.1"
    const val compose_jb = "1.3.0"

    object AndroidX {
        const val activity = "1.6.1"
    }
}
