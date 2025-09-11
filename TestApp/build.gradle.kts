plugins {
    kotlin("jvm")
    id("io.github.arya458.dead-code-detector")
}

group = "io.github.arya458"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("io.socket:socket.io-client:2.1.1")
}

deadCodeDetector {
    failOnDeadCode = true
    includeTests = false          // now includes test classes + test resources
    keepPublicApi = false

    //todo : clearAllDeadCode = true

    includeResources = false      // scan resources
    resourceDir = "src/main/resources"
    testResourceDir = "src/test/res"

    excludePackages.add("com.mycompany.generated")

    keepAnnotations.add("javax.inject.Inject")
}


tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(20)
}