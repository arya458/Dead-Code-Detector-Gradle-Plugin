package com.aria.danesh.model

data class DependencyAnalyzerModel (
    val declaredDeps: Set<String>,
    val usedDeps: Set<String>,
    val deadDeps: Set<String>
){
    fun hasUnusedDependencies(): Boolean = deadDeps.isNotEmpty()
}