package io.github.arya458.model

import io.github.arya458.model.ref.FieldRef
import io.github.arya458.model.ref.MethodRef

data class DeadCodeModel(
    val deadMethods: List<MethodRef>,
    val deadFields: List<FieldRef>,
    val deadClasses: List<String>,
    val deadResources: Set<Pair<String, String>>
) {
    fun hasDeadCode(): Boolean =
        deadMethods.isNotEmpty() || deadFields.isNotEmpty() || deadClasses.isNotEmpty() || deadResources.isNotEmpty()
}