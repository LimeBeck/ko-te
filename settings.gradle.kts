rootProject.name = "ko-te"

pluginManagement {
    val kotlinVersion: String by settings
    plugins {
        kotlin("multiplatform") version kotlinVersion
    }
}
