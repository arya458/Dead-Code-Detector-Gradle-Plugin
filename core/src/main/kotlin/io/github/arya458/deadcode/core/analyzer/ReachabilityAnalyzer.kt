package io.github.arya458.deadcode.core.analyzer

import io.github.arya458.deadcode.core.model.DeadCodeModel
import io.github.arya458.deadcode.core.model.EntryPoint

interface ReachabilityAnalyzer {
    fun findUnreachable(
        graph: CallGraph,
        entryPoints: Set<EntryPoint>
    ): DeadCodeModel
}
