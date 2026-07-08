package io.github.arya458.model

import io.github.arya458.model.ref.MethodRef

data class ResourceModel(
    val declared: Set<Pair<String, String>>,
    val referenced: Set<Pair<String, String>>,
    val referencedClassesFromManifest: Set<String> = emptySet(),
    val referencedClassesFromSpringConfig: Set<String> = emptySet(),
    val referencedMethodsFromXml: Set<MethodRef> = emptySet()
)