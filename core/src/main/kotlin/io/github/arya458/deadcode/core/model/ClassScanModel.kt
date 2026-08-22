package io.github.arya458.deadcode.core.model

import io.github.arya458.deadcode.core.model.ref.FieldRef
import io.github.arya458.deadcode.core.model.ref.MethodRef

data class ClassScanModel(
    val declaredMethods: Set<MethodRef>,
    val declaredFields: Set<FieldRef>,
    val declaredClasses: Set<String>,
    val classAnnotations: Map<String, Set<String>>,
    val referencedMethods: Set<MethodRef>,
    val referencedFields: Set<FieldRef>,
    val referencedClasses: Set<String>
) : java.io.Serializable
