package io.github.arya458.deadcode.core.platform

import io.github.arya458.deadcode.core.model.ref.FieldRef
import io.github.arya458.deadcode.core.model.ref.MethodRef

/**
 * Strategy interface for platform-specific keep rules.
 * Implementations live in platform modules (android, spring, kmp, ktor).
 */
interface PlatformKeepRules {

    fun shouldKeepClass(
        className: String,
        classAnnotations: Map<String, Set<String>>
    ): Boolean

    fun shouldKeepMethod(
        method: MethodRef,
        classAnnotations: Map<String, Set<String>>
    ): Boolean

    fun shouldKeepField(field: FieldRef): Boolean

    fun shouldKeepResource(resourceType: String, resourceName: String): Boolean
}
