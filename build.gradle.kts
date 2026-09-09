import org.gradle.api.attributes.java.TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("maven-publish")
    id("io.github.ben-manes.versions") version "0.61.0"
    id("com.vanniktech.maven.publish.base") version "0.37.0"
}

val kotlinCoroutinesVersion = providers.gradleProperty("kotlinCoroutinesVersion").get()
val junitVersion = providers.gradleProperty("junitVersion").get()

group = "dev.limebeck"
version = providers.gradleProperty("releaseVersion").orElse("0.2.5").get()

repositories {
    mavenCentral()
}

val nativeArtifactId = when {
    System.getProperty("os.name") == "Mac OS X" -> "ko-te-native-macos"
    System.getProperty("os.name") == "Linux" -> "ko-te-native-linux"
    System.getProperty("os.name").startsWith("Windows") -> "ko-te-native-win"
    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        compilations.named("test") {
            compileTaskProvider.configure {
                compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
            }
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    js {
        nodejs()
    }

    when (nativeArtifactId) {
        "ko-te-native-macos" -> macosX64("native")
        "ko-te-native-linux" -> linuxX64("native")
        else -> mingwX64("native")
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutinesVersion")
        }
        jvmTest.dependencies {
            implementation(project.dependencies.platform("org.junit:junit-bom:$junitVersion"))
            implementation(kotlin("test-junit5"))
            runtimeOnly("org.junit.jupiter:junit-jupiter-engine")
            runtimeOnly("org.junit.platform:junit-platform-launcher")
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(if (name == "compileJvmTestJava") 21 else 11)
}

// Publish the runtime requirement without imposing it on JUnit's own dependencies.
configurations.matching {
    it.name in setOf("jvmApiElements", "jvmRuntimeElements", "jvmApiElements-published", "jvmRuntimeElements-published")
}.configureEach {
    attributes.attribute(TARGET_JVM_VERSION_ATTRIBUTE, 11)
}

val stubJavaDocJar = tasks.register<Jar>("stubJavaDocJar") {
    archiveClassifier.value("javadoc")
}

publishing {
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
        withType<MavenPublication>().configureEach {
            artifact(stubJavaDocJar)
            artifactId = when (name) {
                "kotlinMultiplatform" -> "ko-te"
                "jvm" -> "ko-te-jvm"
                "js" -> "ko-te-js"
                "native" -> nativeArtifactId
                else -> artifactId
            }
            val publicationName = this.name
            pom {
                name.set(if (publicationName == "kotlinMultiplatform") "Ko-Te" else "Ko-Te $publicationName")
                description.set("Ko-Te template library ($publicationName)")
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

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
}
