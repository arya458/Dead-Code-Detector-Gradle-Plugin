plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.1.0"
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

repositories {
    mavenCentral()
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
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
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