package io.github.arya458.deadcode.core.model

import io.github.arya458.deadcode.core.model.ref.MethodRef

data class ResourceModel(
    val declared: Set<Pair<String, String>> = emptySet(),
    val referenced: Set<Pair<String, String>> = emptySet(),
    val referencedClassesFromManifest: Set<String> = emptySet(),
    val referencedClassesFromSpringConfig: Set<String> = emptySet(),
    val referencedMethodsFromXml: Set<MethodRef> = emptySet()
)
