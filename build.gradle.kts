plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

allprojects {
    group = "io.github.arya458"
    version = "0.1.0-early"
}

subprojects {
    repositories {
        mavenCentral()
        google()
    }
}
