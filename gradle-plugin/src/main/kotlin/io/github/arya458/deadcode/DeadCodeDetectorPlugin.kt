package io.github.arya458.deadcode

import io.github.arya458.deadcode.task.DeadCodeDetectorTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Entry point of the Dead Code Detector Gradle plugin (new modular architecture).
 *
 * This class only wires the Extension and Task.
 * All analysis logic lives in the `core` module.
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
        ) {
            group = "verification"
            description = "Detect dead code, unused resources and dependencies"
            this.extension = extension
        }

        // Soft dependency on compile tasks (refined later)
        project.afterEvaluate {
            val compileTasks = project.tasks.matching { t ->
                t.name.startsWith("compile") || t.name == "classes"
            }
            if (compileTasks.isNotEmpty()) {
                taskProvider.configure { dependsOn(compileTasks) }
            }
        }

        project.plugins.withId("java") {
            project.tasks.named("check").configure {
                dependsOn("deadCodeDetector")
            }
        }
    }
}
