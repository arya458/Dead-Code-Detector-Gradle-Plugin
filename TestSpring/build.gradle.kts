plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.github.arya458.dead-code-detector")
}

group = "com.aria.danesh"
version = "0.0.1-SNAPSHOT"
description = "TestSpring"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

deadCodeDetector {
    keepAnnotations.add("org.springframework.boot.autoconfigure.SpringBootApplication")

    failOnDeadCode = true
    failOnUnusedDependencies = true
    includeTests = false
    keepPublicApi = true
    platform = "spring"
    includeResources = true
    analyzeDependencies = true
    parallelScan = true
    enableCaching = true
    includeOnlyPackages.add("com.aria.danesh")
}
