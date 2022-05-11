import org.gradle.api.JavaVersion

object Versions {
    object Android {
        const val min = 21
        const val compile = 32
        const val target = compile
        const val buildTools = "32.0.0"
    }

    object Kotlin {
        const val lang = "1.6.21"
        const val coroutines = "1.6.1"
    }

    object Java {
        const val jvmTarget = "17"
        val java = JavaVersion.VERSION_17
    }

    const val spotless = "6.5.0"
    const val ktlint = "0.45.2"
    const val compose_jb = "1.2.0-alpha01-dev683"

    object AndroidX {
        const val activity = "1.4.0"
    }
}
