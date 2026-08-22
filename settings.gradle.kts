pluginManagement {
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
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "dead-code-detector"

// New modular architecture (branch: early)
include("core")
include("platforms:android")
include("platforms:kmp")
include("platforms:spring")
include("platforms:ktor")
include("gradle-plugin")

// Legacy module – kept temporarily during migration
includeBuild("Plugin")

// Existing test / sample projects
include("TestApp")
include("TestSpring")
