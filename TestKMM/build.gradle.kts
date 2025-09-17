import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("io.github.arya458.dead-code-detector")
}

group = "io.github.arya458"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)

}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "TestKMM"
            packageVersion = "1.0.0"
        }
    }
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
