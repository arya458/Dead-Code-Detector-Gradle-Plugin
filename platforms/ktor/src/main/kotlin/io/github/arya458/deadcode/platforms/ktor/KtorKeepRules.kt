package io.github.arya458.deadcode.platforms.ktor

import io.github.arya458.deadcode.core.model.ref.FieldRef
import io.github.arya458.deadcode.core.model.ref.MethodRef
import io.github.arya458.deadcode.core.platform.PlatformKeepRules

/**
 * Keep rules for Ktor applications.
 * Future work: detect Application.module(), routing { }, and application.conf entries.
 */
class KtorKeepRules : PlatformKeepRules {

    private val ktorEntryHints = setOf(
        "module", "install", "routing", "get", "post", "put", "delete", "patch",
        "route", "intercept", "handle"
    )

    override fun shouldKeepMethod(
        method: MethodRef,
        classAnnotations: Map<String, Set<String>>
    ): Boolean = method.name in ktorEntryHints

    override fun shouldKeepField(field: FieldRef): Boolean = false

    override fun shouldKeepClass(
        className: String,
        classAnnotations: Map<String, Set<String>>
    ): Boolean = false

    override fun shouldKeepResource(resourceType: String, resourceName: String): Boolean = false
}
