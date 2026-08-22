plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.gradle.plugin.publish)
}

group = "io.github.arya458"
version = "0.1.0-early"

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(gradleApi())

    implementation(project(":core"))
    implementation(project(":platforms:android"))
    implementation(project(":platforms:kmp"))
    implementation(project(":platforms:spring"))
    implementation(project(":platforms:ktor"))

    implementation(libs.kotlin.stdlib)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    testImplementation(gradleTestKit())
}

gradlePlugin {
    website.set("https://github.com/arya458/Dead-Code-Detector-Gradle-Plugin")
    vcsUrl.set("https://github.com/arya458/Dead-Code-Detector-Gradle-Plugin")

    plugins {
        create("deadCodeDetectorPlugin") {
            id = "io.github.arya458.dead-code-detector"
            implementationClass = "io.github.arya458.deadcode.DeadCodeDetectorPlugin"
            displayName = "Dead Code Detector"
            description = "Detects unused classes, methods, fields, resources and dependencies (Android, KMP, Spring, Ktor)."
            tags.set(listOf("deadcode", "static-analysis", "android", "kmp", "spring", "ktor"))
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
