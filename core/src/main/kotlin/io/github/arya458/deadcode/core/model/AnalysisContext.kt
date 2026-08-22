package io.github.arya458.deadcode.core.model

/**
 * Platform-agnostic context passed to detectors and analyzers.
 * Gradle-specific details are adapted in the gradle-plugin module.
 */
data class AnalysisContext(
    val classScan: ClassScanModel,
    val resourceRoots: List<String> = emptyList(),
    val configDirs: List<String> = emptyList(),
    val platformHint: String = "auto",
    val extra: Map<String, Any> = emptyMap()
)
