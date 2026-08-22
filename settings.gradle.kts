pluginManagement {
    // Local plugin development: resolve id from the included build
    includeBuild("gradle-plugin")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
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
    }
}

rootProject.name = "dead-code-detector"

// Sample / integration projects only at root.
// Plugin + core + platforms live in the included build: gradle-plugin/
include("TestApp")
include("TestSpring")
