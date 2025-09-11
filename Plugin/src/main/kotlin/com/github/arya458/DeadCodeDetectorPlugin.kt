package com.github.arya458

import com.github.arya458.task.DeadCodeDetectorTask
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
            description = "Detect potentially dead classes/methods/fields in compiled output"
            extension = ext
            dependsOn("classes")
        }

        // Optional: run automatically on check
        project.tasks.named("check").configure {
            dependsOn("deadCodeDetector")
        }
    }
}

