package io.github.arya458

import io.github.arya458.model.ref.MethodRef
import java.util.regex.Pattern

open class DeadCodeDetectorExtension {
    // General
    var failOnDeadCode: Boolean = false
    var includeTests: Boolean = false
    var keepPublicApi: Boolean = true

    // Platform detection
    var platform: String = "auto"

    // Custom keep annotations
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

    // Exclusion rules
    val excludePackages: MutableList<String> = mutableListOf()
    val excludeClasses: MutableList<String> = mutableListOf()
    val excludeMethods: MutableList<Pattern> = mutableListOf()
    val excludeFields: MutableList<Pattern> = mutableListOf()

    // Performance
    var parallelScan: Boolean = true

    // Caching
    var enableCaching: Boolean = true

    // Limit scan to specific packages (empty = all)
    var includeOnlyPackages: MutableList<String> = mutableListOf()

    // Advanced custom keep rule
    var customKeepRules: (String, Map<String, Set<String>>, MethodRef?) -> Boolean = { _, _, _ -> false }
}