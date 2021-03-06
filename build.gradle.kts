plugins {
    kotlin("multiplatform") version "1.6.21"
    kotlin("plugin.serialization") version "1.6.21"
}

group = "com.cozmicgames"
version = "0.1.0"

apply(plugin = "maven-publish")

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
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
            }
        }

        val desktopMain by getting {
            dependencies {
                val lwjglVersion = "3.3.0"
                val lwjglNatives = arrayOf("linux", "macos", "windows")
                val lwjglLibs = arrayOf("glfw", "openal", "opengl")

                implementation("org.lwjgl:lwjgl:3.3.0")

                lwjglLibs.forEach { library ->
                    implementation("org.lwjgl:lwjgl-$library:$lwjglVersion")
                }

                lwjglNatives.forEach { nativeTarget ->
                    runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:natives-$nativeTarget")

                    lwjglLibs.forEach { library ->
                        runtimeOnly("org.lwjgl:lwjgl-$library:$lwjglVersion:natives-$nativeTarget")
                    }
                }

                implementation("net.java.jinput:jinput:2.0.9")
                implementation("net.java.jinput:jinput:2.0.9:natives-all")
            }
        }
        val webMain by getting
    }
}
