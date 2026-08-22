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
    compileOnly(localGroovy())

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

tasks.test {
    useJUnitPlatform()
}
