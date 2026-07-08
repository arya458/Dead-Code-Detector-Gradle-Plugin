package io.github.arya458.analysis.platform

import io.github.arya458.model.ref.MethodRef
import io.github.arya458.model.ref.FieldRef

interface PlatformKeepRules {
    fun shouldKeepMethod(method: MethodRef, classAnnotations: Map<String, Set<String>>): Boolean
    fun shouldKeepField(field: FieldRef): Boolean
    fun shouldKeepClass(className: String, classAnnotations: Map<String, Set<String>>): Boolean
    fun shouldKeepResource(resourceType: String, resourceName: String): Boolean
}