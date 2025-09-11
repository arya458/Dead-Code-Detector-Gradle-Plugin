package com.aria.danesh.model

import com.aria.danesh.model.ref.FieldRef
import com.aria.danesh.model.ref.MethodRef

data class DeadCodeModel(
    val deadMethods: List<MethodRef>,
    val deadFields: List<FieldRef>,
    val deadClasses: List<String>,
    val deadResources: Set<Pair<String, String>>
) {
    fun hasDeadCode(): Boolean =
        deadMethods.isNotEmpty() || deadFields.isNotEmpty() || deadClasses.isNotEmpty() || deadResources.isNotEmpty()
}