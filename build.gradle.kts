plugins {
    kotlin("multiplatform") version "1.6.0-RC2"
    kotlin("plugin.serialization") version "1.6.0-RC2"
}

group = "com.gratedgames"
version = "0.1.0"

apply(plugin = "maven-publish")

repositories {
    mavenCentral()
    jcenter()
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
        val commonMain by getting
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

                //implementation("com.github.WilliamAHartman:Jamepad:1.4.0")
                //implementation("uk.co.electronstudio.sdl2gdx:sdl2gdx:1.0.4")
            }
        }
        val webMain by getting
    }
}