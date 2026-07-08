package io.github.arya458

open class DeadCodeDetectorExtension {
    // General
    var failOnDeadCode: Boolean = false
    var includeTests: Boolean = false
    var keepPublicApi: Boolean = true

    // Platform detection: "android", "spring", "kmm", "auto" (default)
    var platform: String = "auto"

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
    val excludeMethods: MutableList<Regex> = mutableListOf()
    val excludeFields: MutableList<Regex> = mutableListOf()
    val keepAnnotations: MutableList<String> = mutableListOf()

    // Performance
    var parallelScan: Boolean = true
}