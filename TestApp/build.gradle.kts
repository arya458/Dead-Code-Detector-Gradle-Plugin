plugins {
    kotlin("jvm")
    id("io.github.arya458.dead-code-detector")
}

group = "io.github.arya458"
version = "1.0-SNAPSHOT"

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("io.socket:socket.io-client:2.1.1")
}

deadCodeDetector {
    failOnDeadCode = true
    includeTests = false
    keepPublicApi = false

    includeResources = false
    resourceDir = "src/main/resources"
    testResourceDir = "src/test/res"

    excludePackages.add("com.mycompany.generated")
    keepAnnotations.add("javax.inject.Inject")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
