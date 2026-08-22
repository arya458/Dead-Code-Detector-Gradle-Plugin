// Root: samples aggregator only.
// Plugin implementation is in the included build `gradle-plugin/`.

plugins {
    // Declare once so subprojects can apply without version
    kotlin("jvm") version "2.2.21" apply false
    kotlin("plugin.spring") version "2.2.21" apply false
}

allprojects {
    group = "io.github.arya458"
    version = "0.1.0-early"
}
