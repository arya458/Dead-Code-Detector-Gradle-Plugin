plugins {
    kotlin("jvm") version "2.0.21"
}

// Local multi-project: load plugin from :gradle-plugin (plugins {} DSL needs publish/includeBuild)
buildscript {
    dependencies {
        classpath(project(":gradle-plugin"))
    }
}
apply(plugin = "io.github.arya458.dead-code-detector")

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
