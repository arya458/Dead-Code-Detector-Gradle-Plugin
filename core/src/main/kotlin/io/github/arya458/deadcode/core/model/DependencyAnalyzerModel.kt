package io.github.arya458.deadcode.core.model

data class DependencyAnalyzerModel(
    val declaredDeps: Set<String> = emptySet(),
    val usedDeps: Set<String> = emptySet(),
    val deadDeps: Set<String> = emptySet()
) {
    fun hasUnusedDependencies(): Boolean = deadDeps.isNotEmpty()
}
