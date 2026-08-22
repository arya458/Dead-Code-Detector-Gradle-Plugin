package io.github.arya458.deadcode.core.platform

import io.github.arya458.deadcode.core.model.AnalysisContext

/**
 * Central registry for platform-specific strategies.
 * Platform modules register themselves at startup.
 */
object PlatformRegistry {

    private val keepRules = mutableListOf<PlatformKeepRules>()
    private val entryPointDetectors = mutableListOf<EntryPointDetector>()

    fun register(rules: PlatformKeepRules) {
        keepRules += rules
    }

    fun register(detector: EntryPointDetector) {
        entryPointDetectors += detector
    }

    fun allKeepRules(): List<PlatformKeepRules> = keepRules.toList()

    fun allEntryPointDetectors(): List<EntryPointDetector> = entryPointDetectors.toList()

    fun clear() {
        keepRules.clear()
        entryPointDetectors.clear()
    }
}
