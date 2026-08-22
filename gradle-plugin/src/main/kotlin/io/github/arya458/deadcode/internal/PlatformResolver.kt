package io.github.arya458.deadcode.internal

import io.github.arya458.deadcode.core.platform.CompositeKeepRules
import io.github.arya458.deadcode.core.platform.PlatformKeepRules
import io.github.arya458.deadcode.platforms.android.AndroidKeepRules
import io.github.arya458.deadcode.platforms.kmp.KmpKeepRules
import io.github.arya458.deadcode.platforms.ktor.KtorKeepRules
import io.github.arya458.deadcode.platforms.spring.SpringKeepRules
import org.gradle.api.Project

object PlatformResolver {

    fun resolve(project: Project, platformHint: String): PlatformKeepRules {
        val hint = platformHint.lowercase()
        if (hint != "auto") {
            return when (hint) {
                "android" -> AndroidKeepRules()
                "spring" -> SpringKeepRules()
                "kmp", "kmm" -> KmpKeepRules()
                "ktor" -> KtorKeepRules()
                else -> KmpKeepRules()
            }
        }

        val detected = mutableListOf<PlatformKeepRules>()

        if (project.plugins.hasPlugin("com.android.application") ||
            project.plugins.hasPlugin("com.android.library")
        ) {
            detected += AndroidKeepRules()
        }
        if (project.plugins.hasPlugin("org.springframework.boot") ||
            project.plugins.hasPlugin("io.spring.dependency-management")
        ) {
            detected += SpringKeepRules()
        }
        if (project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
            detected += KmpKeepRules()
        }
        // Ktor has no dedicated plugin id – detect via dependency later if needed
        if (project.configurations.any { cfg ->
                runCatching {
                    cfg.dependencies.any { d ->
                        d.group?.startsWith("io.ktor") == true
                    }
                }.getOrDefault(false)
            }
        ) {
            detected += KtorKeepRules()
        }

        if (detected.isEmpty()) {
            // safe default
            detected += KmpKeepRules()
        }

        return if (detected.size == 1) detected.first() else CompositeKeepRules(detected)
    }
}
