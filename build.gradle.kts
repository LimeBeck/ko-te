plugins {
    kotlin("multiplatform")
    id("maven-publish")
    id("com.github.ben-manes.versions").version("0.42.0")
    signing
}

val kotlinCoroutinesVersion: String by project

group = "dev.limebeck"
version = "0.2.5"

repositories {
    mavenCentral()
}

kotlin {
    metadata {
        mavenPublication {
            artifactId = "ko-te"
            pom {
                name.set("Ko-Te template library metadata")
                description.set("Kotlin metadata module for Ko-Te template library")
            }
        }
    }
    jvm {
        mavenPublication {
            artifactId = "ko-te-jvm"
            pom {
                name.set("Ko-Te template library JVM")
                description.set("Kotlin JVM module for Ko-Te template library")
            }
        }
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    js(IR) {
        mavenPublication {
            artifactId = "ko-te-js"
            pom {
                name.set("Ko-Te template library JS")
                description.set("Kotlin JS module for Ko-Te template library")
            }
        }
        binaries.executable()
        nodejs {
        }
    }

    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native") {
            mavenPublication {
                artifactId = "ko-te-native-macos"
                pom {
                    name.set("Ko-Te template library native-macos")
                    description.set("Kotlin native-macos module for Ko-Te template library")
                }
            }
        }

        hostOs == "Linux" -> linuxX64("native") {
            mavenPublication {
                artifactId = "ko-te-native-linux"
                pom {
                    name.set("Ko-Te template library native-linux")
                    description.set("Kotlin native-linux module for Ko-Te template library")
                }
            }
        }

        isMingwX64 -> mingwX64("native") {
            mavenPublication {
                artifactId = "ko-te-native-win"
                pom {
                    name.set("Ko-Te template library native-win")
                    description.set("Kotlin native-win module for Ko-Te template library")
                }
            }
        }

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

val stubJavaDocJar by tasks.registering(Jar::class) {
    archiveClassifier.value("javadoc")
}

publishing {
    kotlin.targets.forEach { target ->
        val targetPublication: Publication? = publications.findByName(target.name)
        if (targetPublication is MavenPublication) {
            targetPublication.artifact(stubJavaDocJar.get())
        }
    }

    repositories {
        maven {
            name = "MainRepo"
            url = uri(
                System.getenv("REPO_URI")
                    ?: project.findProperty("repo.uri") as String
            )
            credentials {
                username = System.getenv("REPO_USERNAME")
                    ?: project.findProperty("repo.username") as String?
                password = System.getenv("REPO_PASSWORD")
                    ?: project.findProperty("repo.password") as String?
            }
        }
    }

    publications {
        withType<MavenPublication> {
            val publicationName = this.name
            pom {
                if (publicationName == "kotlinMultiplatform") {
                    name.set("Ko-Te")
                    description.set("Ko-Te template library")
                }
                groupId = "dev.limebeck"
                url.set("https://github.com/LimeBeck/ko-te")
                developers {
                    developer {
                        id.set("LimeBeck")
                        name.set("Anatoly Nechay-Gumen")
                        email.set("mail@limebeck.dev")
                    }
                }
                licenses {
                    license {
                        name.set("MIT license")
                        url.set("https://github.com/LimeBeck/ko-te/blob/master/LICENCE")
                        distribution.set("repo")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/LimeBeck/ko-te.git")
                    developerConnection.set("scm:git:ssh://github.com/LimeBeck/ko-te.git")
                    url.set("https://github.com/LimeBeck/ko-te")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications)
}