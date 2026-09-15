plugins {
    kotlin("multiplatform") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
}

group = "io.github.cozmicgames"
version = "0.1.0"

apply(plugin = "maven-publish")

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

object DependencyVersions {
    const val lwjgl = "3.4.3"
    const val kotlinSerialization = "1.7.3"
}

kotlin {
    jvm("desktop") {
    }
    js("web") {
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
                implementation("org.lwjgl:lwjgl:${DependencyVersions.lwjgl}")
                implementation("org.lwjgl:lwjgl:${DependencyVersions.lwjgl}:natives-linux")
                implementation("org.lwjgl:lwjgl:${DependencyVersions.lwjgl}:natives-macos")
                implementation("org.lwjgl:lwjgl:${DependencyVersions.lwjgl}:natives-windows")

                implementation("org.lwjgl:lwjgl-glfw:${DependencyVersions.lwjgl}")
                implementation("org.lwjgl:lwjgl-glfw:${DependencyVersions.lwjgl}:natives-linux")
                implementation("org.lwjgl:lwjgl-glfw:${DependencyVersions.lwjgl}:natives-macos")
                implementation("org.lwjgl:lwjgl-glfw:${DependencyVersions.lwjgl}:natives-windows")

                implementation("org.lwjgl:lwjgl-openal:${DependencyVersions.lwjgl}")
                implementation("org.lwjgl:lwjgl-openal:${DependencyVersions.lwjgl}:natives-linux")
                implementation("org.lwjgl:lwjgl-openal:${DependencyVersions.lwjgl}:natives-macos")
                implementation("org.lwjgl:lwjgl-openal:${DependencyVersions.lwjgl}:natives-windows")

                implementation("org.lwjgl:lwjgl-shaderc:${DependencyVersions.lwjgl}")
                implementation("org.lwjgl:lwjgl-shaderc:${DependencyVersions.lwjgl}:natives-linux")
                implementation("org.lwjgl:lwjgl-shaderc:${DependencyVersions.lwjgl}:natives-macos")
                implementation("org.lwjgl:lwjgl-shaderc:${DependencyVersions.lwjgl}:natives-windows")

                implementation("org.lwjgl:lwjgl-spvc:${DependencyVersions.lwjgl}")
                implementation("org.lwjgl:lwjgl-spvc:${DependencyVersions.lwjgl}:natives-linux")
                implementation("org.lwjgl:lwjgl-spvc:${DependencyVersions.lwjgl}:natives-macos")
                implementation("org.lwjgl:lwjgl-spvc:${DependencyVersions.lwjgl}:natives-windows")

                implementation("org.lwjgl:lwjgl-opengl:${DependencyVersions.lwjgl}")
                implementation("org.lwjgl:lwjgl-opengl:${DependencyVersions.lwjgl}:natives-linux")
                implementation("org.lwjgl:lwjgl-opengl:${DependencyVersions.lwjgl}:natives-macos")
                implementation("org.lwjgl:lwjgl-opengl:${DependencyVersions.lwjgl}:natives-windows")

                implementation("org.lwjgl:lwjgl-vulkan:${DependencyVersions.lwjgl}")
                implementation("org.lwjgl:lwjgl-vulkan:${DependencyVersions.lwjgl}:natives-macos")

                implementation("org.lwjgl:lwjgl-vma:${DependencyVersions.lwjgl}")
                implementation("org.lwjgl:lwjgl-vma:${DependencyVersions.lwjgl}:natives-linux")
                implementation("org.lwjgl:lwjgl-vma:${DependencyVersions.lwjgl}:natives-macos")
                implementation("org.lwjgl:lwjgl-vma:${DependencyVersions.lwjgl}:natives-windows")

                implementation("org.lwjgl:lwjgl-tinyfd:${DependencyVersions.lwjgl}")
                implementation("org.lwjgl:lwjgl-tinyfd:${DependencyVersions.lwjgl}:natives-linux")
                implementation("org.lwjgl:lwjgl-tinyfd:${DependencyVersions.lwjgl}:natives-macos")
                implementation("org.lwjgl:lwjgl-tinyfd:${DependencyVersions.lwjgl}:natives-windows")

                implementation("fr.delthas:javamp3:1.0.1")
            }
        }
        val webMain by getting
    }
}
