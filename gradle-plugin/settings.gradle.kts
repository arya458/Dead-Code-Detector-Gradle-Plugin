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

// Library modules live one level up; map them into this included build
include(":core")
project(":core").projectDir = file("../core")

include(":platforms:android")
project(":platforms:android").projectDir = file("../platforms/android")

include(":platforms:kmp")
project(":platforms:kmp").projectDir = file("../platforms/kmp")

include(":platforms:spring")
project(":platforms:spring").projectDir = file("../platforms/spring")

include(":platforms:ktor")
project(":platforms:ktor").projectDir = file("../platforms/ktor")

// This project (gradle-plugin) is the root of the included build
