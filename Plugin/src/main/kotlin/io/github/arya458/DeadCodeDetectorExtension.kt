package io.github.arya458

import io.github.arya458.model.ref.MethodRef
import java.util.regex.Pattern

open class DeadCodeDetectorExtension {
    // General
    var failOnDeadCode: Boolean = false
    var includeTests: Boolean = false
    var keepPublicApi: Boolean = true

    // Platform detection: "android", "spring", "kmm", "auto" (default)
    var platform: String = "auto"

    // Custom keep annotations (user can add more)
    var keepAnnotations: MutableList<String> = mutableListOf(
        "javax.persistence.Entity",
        "javax.persistence.MappedSuperclass",
        "javax.persistence.Embeddable"
    )

    // Resource scanning
    var includeResources: Boolean = true
    var resourceDir: String = "src/main/res"
    var testResourceDir: String = "src/test/res"

    // Dependency scanning
    var analyzeDependencies: Boolean = true
    var failOnUnusedDependencies: Boolean = false

    // Spring specific
    var scanConfigFiles: Boolean = true
    var configDirs: List<String> = listOf("src/main/resources")

    // Exclusion rules (now support regex too)
    val excludePackages: MutableList<String> = mutableListOf()
    val excludeClasses: MutableList<String> = mutableListOf()
    val excludeMethods: MutableList<Pattern> = mutableListOf()
    val excludeFields: MutableList<Pattern> = mutableListOf()

    // Performance
    var parallelScan: Boolean = true

    // Advanced: custom keep rule provider (for advanced users)
    var customKeepRules: (String, Map<String, Set<String>>, MethodRef?) -> Boolean = { _, _, _ -> false }
}