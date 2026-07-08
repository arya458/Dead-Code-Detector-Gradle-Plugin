package io.github.arya458.analysis.platform

import io.github.arya458.model.ref.MethodRef
import io.github.arya458.model.ref.FieldRef

class KmmKeepRules : PlatformKeepRules {
    override fun shouldKeepMethod(method: MethodRef, classAnnotations: Map<String, Set<String>>): Boolean {
        return false
    }

    override fun shouldKeepField(field: FieldRef): Boolean = false

    override fun shouldKeepClass(className: String, classAnnotations: Map<String, Set<String>>): Boolean {
        return false
    }

    override fun shouldKeepResource(resourceType: String, resourceName: String): Boolean = false
}