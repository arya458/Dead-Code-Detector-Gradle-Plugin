package io.github.arya458.analysis.platform

import io.github.arya458.model.ref.MethodRef
import io.github.arya458.model.ref.FieldRef

class SpringKeepRules : PlatformKeepRules {
    private val webAnnotations = setOf(
        "org.springframework.web.bind.annotation.RequestMapping",
        "org.springframework.web.bind.annotation.GetMapping",
        "org.springframework.web.bind.annotation.PostMapping",
        "org.springframework.web.bind.annotation.PutMapping",
        "org.springframework.web.bind.annotation.DeleteMapping"
    )

    private val componentAnnotations = setOf(
        "org.springframework.stereotype.Service",
        "org.springframework.stereotype.Controller",
        "org.springframework.stereotype.Repository",
        "org.springframework.stereotype.Component",
        "org.springframework.web.bind.annotation.RestController",
        "org.springframework.boot.autoconfigure.SpringBootApplication",
        "org.springframework.context.annotation.Configuration"
    )

    override fun shouldKeepMethod(method: MethodRef, classAnnotations: Map<String, Set<String>>): Boolean {
        return method.annotations.any { webAnnotations.contains(it) }
    }

    override fun shouldKeepField(field: FieldRef): Boolean = false

    override fun shouldKeepClass(className: String, classAnnotations: Map<String, Set<String>>): Boolean {
        val anns = classAnnotations[className] ?: emptySet()
        return anns.any { componentAnnotations.contains(it) }
    }

    override fun shouldKeepResource(resourceType: String, resourceName: String): Boolean = false
}