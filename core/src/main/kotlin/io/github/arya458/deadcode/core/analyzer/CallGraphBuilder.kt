package io.github.arya458.deadcode.core.analyzer

import io.github.arya458.deadcode.core.model.ClassScanModel

/**
 * Builds a call/reference graph from a ClassScanModel.
 * Future implementation will support full reachability analysis.
 */
interface CallGraphBuilder {
    fun build(scan: ClassScanModel): CallGraph
}

data class CallGraph(
    val nodes: Set<String>,
    val edges: Map<String, Set<String>> // from -> to
)
