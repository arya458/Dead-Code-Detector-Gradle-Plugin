package io.github.arya458.deadcode

import io.github.arya458.deadcode.task.DeadCodeDetectorTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Entry point of the Dead Code Detector Gradle plugin (modular architecture).
 * Wires Extension + Task; analysis lives in `core`.
 */
class DeadCodeDetectorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create(
            "deadCodeDetector",
            DeadCodeDetectorExtension::class.java
        )

        val taskProvider = project.tasks.register(
            "deadCodeDetector",
            DeadCodeDetectorTask::class.java
        ) { task ->
            task.group = "verification"
            task.description = "Detect dead code, unused resources and dependencies"
            task.extension = extension
        }

        project.afterEvaluate {
            val compileTasks = project.tasks.matching { t ->
                t.name.startsWith("compile") || t.name == "classes"
            }
            if (!compileTasks.isEmpty()) {
                taskProvider.configure { task ->
                    task.dependsOn(compileTasks)
                }
            }
        }

        project.plugins.withId("java") {
            project.tasks.named("check").configure { task ->
                task.dependsOn("deadCodeDetector")
            }
        }
        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            project.tasks.named("check").configure { task ->
                task.dependsOn("deadCodeDetector")
            }
        }
    }
}
