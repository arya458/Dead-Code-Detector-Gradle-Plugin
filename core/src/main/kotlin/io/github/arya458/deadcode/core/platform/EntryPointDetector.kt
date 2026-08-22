package io.github.arya458.deadcode.core.platform

import io.github.arya458.deadcode.core.model.EntryPoint
import io.github.arya458.deadcode.core.model.AnalysisContext

/**
 * Detects entry points for a given platform (main, Activities, @RestController, Ktor modules, ...).
 */
fun interface EntryPointDetector {
    fun detect(context: AnalysisContext): Set<EntryPoint>
}
