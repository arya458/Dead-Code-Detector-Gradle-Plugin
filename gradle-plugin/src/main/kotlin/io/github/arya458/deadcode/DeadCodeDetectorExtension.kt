package io.github.arya458.deadcode

import java.util.regex.Pattern

/**
 * User-facing configuration for the dead-code-detector plugin.
 */
open class DeadCodeDetectorExtension {

    var failOnDeadCode: Boolean = false
    var includeTests: Boolean = false
    var keepPublicApi: Boolean = true

    /** auto | android | spring | kmp | ktor */
    var platform: String = "auto"

    var keepAnnotations: MutableList<String> = mutableListOf(
        "javax.persistence.Entity",
        "javax.persistence.MappedSuperclass",
        "javax.persistence.Embeddable"
    )

    var includeResources: Boolean = true
    var resourceDir: String = "src/main/res"
    var testResourceDir: String = "src/test/res"

    var analyzeDependencies: Boolean = true
    var failOnUnusedDependencies: Boolean = false

    var scanConfigFiles: Boolean = true
    var configDirs: List<String> = listOf("src/main/resources")

    val excludePackages: MutableList<String> = mutableListOf()
    val excludeClasses: MutableList<String> = mutableListOf()
    val excludeMethods: MutableList<Pattern> = mutableListOf()
    val excludeFields: MutableList<Pattern> = mutableListOf()

    var parallelScan: Boolean = true
    var enableCaching: Boolean = true

    var includeOnlyPackages: MutableList<String> = mutableListOf()
}
