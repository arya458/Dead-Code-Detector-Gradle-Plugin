package io.github.arya458.deadcode.platforms.spring

import io.github.arya458.deadcode.core.model.ref.FieldRef
import io.github.arya458.deadcode.core.model.ref.MethodRef
import io.github.arya458.deadcode.core.platform.PlatformKeepRules

class SpringKeepRules : PlatformKeepRules {

    private val webAnnotations = setOf(
        "org.springframework.web.bind.annotation.RequestMapping",
        "org.springframework.web.bind.annotation.GetMapping",
        "org.springframework.web.bind.annotation.PostMapping",
        "org.springframework.web.bind.annotation.PutMapping",
        "org.springframework.web.bind.annotation.DeleteMapping",
        "org.springframework.web.bind.annotation.PatchMapping"
    )

    private val componentAnnotations = setOf(
        "org.springframework.stereotype.Service",
        "org.springframework.stereotype.Controller",
        "org.springframework.stereotype.Repository",
        "org.springframework.stereotype.Component",
        "org.springframework.web.bind.annotation.RestController",
        "org.springframework.boot.autoconfigure.SpringBootApplication",
        "org.springframework.context.annotation.Configuration",
        "org.springframework.context.annotation.Bean"
    )

    override fun shouldKeepMethod(
        method: MethodRef,
        classAnnotations: Map<String, Set<String>>
    ): Boolean = method.annotations.any { it in webAnnotations }

    override fun shouldKeepField(field: FieldRef): Boolean = false

    override fun shouldKeepClass(
        className: String,
        classAnnotations: Map<String, Set<String>>
    ): Boolean {
        val anns = classAnnotations[className] ?: emptySet()
        return anns.any { it in componentAnnotations }
    }

    override fun shouldKeepResource(resourceType: String, resourceName: String): Boolean = false
}
