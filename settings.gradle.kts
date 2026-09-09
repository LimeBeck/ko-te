rootProject.name = "ko-te"

pluginManagement {
    val kotlinVersion = providers.gradleProperty("kotlinVersion").get()
    plugins {
        kotlin("multiplatform") version kotlinVersion
    }
}
