package com.github.arya458.model

import com.github.arya458.model.ref.FieldRef
import com.github.arya458.model.ref.MethodRef

data class DeadCodeModel(
    val deadMethods: List<MethodRef>,
    val deadFields: List<FieldRef>,
    val deadClasses: List<String>,
    val deadResources: Set<Pair<String, String>>
) {
    fun hasDeadCode(): Boolean =
        deadMethods.isNotEmpty() || deadFields.isNotEmpty() || deadClasses.isNotEmpty() || deadResources.isNotEmpty()
}