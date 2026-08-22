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
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "dead-code-detector"

include("core")
include("platforms:android")
include("platforms:kmp")
include("platforms:spring")
include("platforms:ktor")
include("gradle-plugin")

include("TestApp")
include("TestSpring")
