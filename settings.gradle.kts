pluginManagement {
    // Local plugin: resolves id("io.github.arya458.dead-code-detector") without version
    includeBuild("gradle-plugin")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

rootProject.name = "dead-code-detector"

// Samples only — plugin + libraries live inside the included build
include("TestApp")
include("TestSpring")
