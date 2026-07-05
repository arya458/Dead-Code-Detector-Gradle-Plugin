package io.github.arya458

/**
 * Extension DSL for configuring the dead code detector plugin.
 * All options are documented for user customization.
 */
open class DeadCodeDetectorExtension {
    // Code scanning options
    var failOnDeadCode: Boolean = false
    var includeTests: Boolean = false
    var keepPublicApi: Boolean = true

    // Resource scanning options
    var includeResources: Boolean = true
    var resourceDir: String = "src/main/res"
    var testResourceDir: String = "src/test/res"

    // Dependency scanning options
    var analyzeDependencies: Boolean = true
    var failOnUnusedDependencies: Boolean = false

    // Exclusion rules (supports package, class, method/field regex)
    val excludePackages: MutableList<String> = mutableListOf()
    val excludeClasses: MutableList<String> = mutableListOf()
    val excludeMethods: MutableList<Regex> = mutableListOf()
    val excludeFields: MutableList<Regex> = mutableListOf()
    val keepAnnotations: MutableList<String> = mutableListOf()

    // Performance
    var parallelScan: Boolean = true
}