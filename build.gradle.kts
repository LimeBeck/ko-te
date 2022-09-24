plugins {
    kotlin("multiplatform")
    id("maven-publish")
    id("com.github.ben-manes.versions").version("0.42.0")
}

val kotlinCoroutinesVersion: String by project

publishing {
    repositories {
        maven {
            name = "MainRepo"
            url = uri(
                project.findProperty("repo.uri") as String?
                    ?: System.getenv("REPO_URI")
            )
            credentials {
                username = project.findProperty("repo.username") as String?
                    ?: System.getenv("REPO_USERNAME")
                password = project.findProperty("repo.password") as String?
                    ?: System.getenv("REPO_PASSWORD")
            }
        }
    }
}

group = "dev.limebeck"
version = "0.2.1"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    js(IR) {
//        useCommonJs()
        binaries.executable()
        nodejs {
        }
    }

    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {

            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${kotlinCoroutinesVersion}") {
                    version {
                        strictly(kotlinCoroutinesVersion)
                    }
                }
            }
        }
        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
        val nativeMain by getting
        val nativeTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
            }
        }
    }
}