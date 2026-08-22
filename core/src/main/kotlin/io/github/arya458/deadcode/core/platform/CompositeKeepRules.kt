package io.github.arya458.deadcode.core.platform

import io.github.arya458.deadcode.core.model.ref.FieldRef
import io.github.arya458.deadcode.core.model.ref.MethodRef

/**
 * Composes multiple platform keep-rule strategies (OR semantics).
 */
class CompositeKeepRules(
    private val delegates: List<PlatformKeepRules>
) : PlatformKeepRules {

    override fun shouldKeepClass(
        className: String,
        classAnnotations: Map<String, Set<String>>
    ): Boolean = delegates.any { it.shouldKeepClass(className, classAnnotations) }

    override fun shouldKeepMethod(
        method: MethodRef,
        classAnnotations: Map<String, Set<String>>
    ): Boolean = delegates.any { it.shouldKeepMethod(method, classAnnotations) }

    override fun shouldKeepField(field: FieldRef): Boolean =
        delegates.any { it.shouldKeepField(field) }

    override fun shouldKeepResource(resourceType: String, resourceName: String): Boolean =
        delegates.any { it.shouldKeepResource(resourceType, resourceName) }
}
