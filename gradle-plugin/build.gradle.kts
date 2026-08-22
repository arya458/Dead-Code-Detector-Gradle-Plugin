plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    `maven-publish`
}

group = "io.github.arya458"
version = "0.1.0-early"

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(gradleApi())

    implementation(project(":core"))
    implementation(project(":platform-android"))
    implementation(project(":platform-kmp"))
    implementation(project(":platform-spring"))
    implementation(project(":platform-ktor"))

    implementation(libs.kotlin.stdlib)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    testImplementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        create("deadCodeDetectorPlugin") {
            id = "io.github.arya458.dead-code-detector"
            implementationClass = "io.github.arya458.deadcode.DeadCodeDetectorPlugin"
            displayName = "Dead Code Detector"
            description = "Detects unused classes, methods, fields, resources and dependencies."
            tags.set(listOf("deadcode", "static-analysis", "android", "kmp", "spring", "ktor"))
        }
    }
}

// Do NOT create a manual MavenPublication named "pluginMaven" —
// java-gradle-plugin already registers it.

tasks.test {
    useJUnitPlatform()
}
