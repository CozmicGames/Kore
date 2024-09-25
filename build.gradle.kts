plugins {
    kotlin("multiplatform") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
}

group = "com.cozmicgames"
version = "1.0.0"

apply(plugin = "maven-publish")

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

object DependencyVersions {
    const val lwjgl = "3.3.4"
    const val kotlinSerialization = "1.7.3"
}

kotlin {
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }
    js("web") {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${DependencyVersions.kotlinSerialization}")
            }
        }

        val desktopMain by getting {
            dependencies {
                val lwjglNatives = arrayOf("linux", "macos", "windows")
                val lwjglLibs = arrayOf("glfw", "openal", "opengl", "tinyfd", "msdfgen")

                implementation("org.lwjgl:lwjgl:${DependencyVersions.lwjgl}")

                lwjglLibs.forEach { library ->
                    implementation("org.lwjgl:lwjgl-$library:${DependencyVersions.lwjgl}")
                }

                lwjglNatives.forEach { nativeTarget ->
                    runtimeOnly("org.lwjgl:lwjgl:${DependencyVersions.lwjgl}:natives-$nativeTarget")

                    lwjglLibs.forEach { library ->
                        runtimeOnly("org.lwjgl:lwjgl-$library:${DependencyVersions.lwjgl}:natives-$nativeTarget")
                    }
                }

                implementation("fr.delthas:javamp3:1.0.1")
            }
        }
        val webMain by getting
    }
    jvmToolchain(11)
}
