package io.github.arya458.deadcode.platforms.kmp

import io.github.arya458.deadcode.core.model.ref.FieldRef
import io.github.arya458.deadcode.core.model.ref.MethodRef
import io.github.arya458.deadcode.core.platform.PlatformKeepRules

/**
 * Keep rules for Kotlin Multiplatform projects.
 * Will be extended to understand commonMain / platform source sets and expect/actual.
 */
class KmpKeepRules : PlatformKeepRules {

    override fun shouldKeepMethod(
        method: MethodRef,
        classAnnotations: Map<String, Set<String>>
    ): Boolean = false

    override fun shouldKeepField(field: FieldRef): Boolean = false

    override fun shouldKeepClass(
        className: String,
        classAnnotations: Map<String, Set<String>>
    ): Boolean = false

    override fun shouldKeepResource(resourceType: String, resourceName: String): Boolean = false
}
