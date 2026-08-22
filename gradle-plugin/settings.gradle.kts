pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "dead-code-detector-gradle-plugin"

// Flat names — nested include(":platforms:android") would require
// gradle-plugin/platforms/ to exist as intermediate project dir.
include(":core")
project(":core").projectDir = file("../core")

include(":platform-android")
project(":platform-android").projectDir = file("../platforms/android")

include(":platform-kmp")
project(":platform-kmp").projectDir = file("../platforms/kmp")

include(":platform-spring")
project(":platform-spring").projectDir = file("../platforms/spring")

include(":platform-ktor")
project(":platform-ktor").projectDir = file("../platforms/ktor")
