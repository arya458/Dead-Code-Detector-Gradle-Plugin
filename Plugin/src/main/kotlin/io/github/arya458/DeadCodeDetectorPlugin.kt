package io.github.arya458

import io.github.arya458.task.DeadCodeDetectorTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class DeadCodeDetectorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create(
            "deadCodeDetector",
            DeadCodeDetectorExtension::class.java
        )

        project.tasks.register("deadCodeDetector", DeadCodeDetectorTask::class.java) {
            group = "verification"
            description = "Detect dead code, resources, and dependencies in Spring, Android, and KMM projects"
            extension = ext
            dependsOn("classes")
        }

        project.tasks.named("check").configure {
            dependsOn("deadCodeDetector")
        }
    }
}