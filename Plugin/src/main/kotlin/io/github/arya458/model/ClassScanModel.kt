package io.github.arya458.model

import io.github.arya458.model.ref.FieldRef
import io.github.arya458.model.ref.MethodRef


data class ClassScanModel(
    val declaredMethods: Set<MethodRef>,
    val declaredFields: Set<FieldRef>,
    val declaredClasses: Set<String>,
    val referencedMethods: Set<MethodRef>,
    val referencedFields: Set<FieldRef>,
    val referencedClasses: Set<String>
)