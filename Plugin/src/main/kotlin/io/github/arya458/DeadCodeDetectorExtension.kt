package io.github.arya458

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

    // Exclusion rules
    val excludePackages: MutableList<String> = mutableListOf()
    val keepAnnotations: MutableList<String> = mutableListOf()
}




