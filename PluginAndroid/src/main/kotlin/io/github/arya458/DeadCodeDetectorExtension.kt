package io.github.arya458

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

interface DeadCodeDetectorExtension {
    val failOnDeadCode: Property<Boolean> // Default: false
    val includeTests: Property<Boolean> // Default: false
    val keepPublicApi: Property<Boolean> // Default: false
    val includeResources: Property<Boolean> // Default: false
    val resourceDir: Property<String> // Default: "src/main/resources"
    val testResourceDir: Property<String> // Default: "src/test/res"
    val excludePackages: ListProperty<String> // Default: emptyList()
    val keepAnnotations: ListProperty<String> // Default: emptyList()
    // val clearAllDeadCode: Property<Boolean> // Default: false (Planned feature)
}
