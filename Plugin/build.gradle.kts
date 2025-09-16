import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.js.inline.util.getImportTag

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.2.1"
}

group = "io.github.arya458"
version = "0.0.6"

repositories {
    mavenCentral()
}

dependencies {
    // Bytecode analysis
    implementation("org.ow2.asm:asm:9.8")
    // Optional: fast classpath scanning
    implementation("io.github.classgraph:classgraph:4.8.181")
    // Kotlin stdlib
    implementation(kotlin("stdlib"))
}

gradlePlugin {
    plugins {
        create("deadCodeDetectorPlugin") {
            id = "io.github.arya458.dead-code-detector"
            implementationClass = "io.github.arya458.DeadCodeDetectorPlugin"
            displayName = "Dead Code Detector"
            tags.set(listOf("deadcode", "detector", "cleanup", "resources", "android", "ktor", "spring"))
            description = "Detects unused classes, methods, fields, and (Android, Ktor,Spring,...) resources in your project."
        }
    }
    website = "https://github.com/arya458/Dead-Code-Detector-Gradle-Plugin"
    vcsUrl = "https://github.com/arya458/Dead-Code-Detector-Gradle-Plugin"
}
