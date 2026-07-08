package io.github.arya458

import io.github.arya458.task.DeadCodeDetectorTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class DeadCodeDetectorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create(
            "deadCodeDetector",
            DeadCodeDetectorExtension::class.java
        )

        val taskProvider = project.tasks.register("deadCodeDetector", DeadCodeDetectorTask::class.java) {
            group = "verification"
            description = "Detect dead code, resources, and dependencies in Spring, Android, and KMM projects"
            extension = ext
        }

        project.afterEvaluate {
            val compileTask = findCompileTask(project)
            if (compileTask != null) {
                taskProvider.configure {
                    dependsOn(compileTask)
                }
                project.logger.info("deadCodeDetector depends on task: ${compileTask.name}")
            } else {
                project.logger.warn(
                    "No suitable compile task found for deadCodeDetector. " +
                            "Make sure the project has been compiled before running this task."
                )
            }
        }

        project.tasks.named("check").configure {
            dependsOn("deadCodeDetector")
        }
    }

    private fun findCompileTask(project: Project): Task? {
        val possibleTaskNames = listOf(
            "classes",
            "compileDebugJavaWithJavac",
            "compileReleaseJavaWithJavac",
            "compileDebugKotlin",
            "compileReleaseKotlin",
            "assembleDebug",
            "assemble"
        )

        for (taskName in possibleTaskNames) {
            val task = project.tasks.findByName(taskName)
            if (task != null) {
                return task
            }
        }

        val compileTasks = project.tasks.matching { it.name.startsWith("compile") }
        if (compileTasks.isNotEmpty()) {
            return compileTasks.first()
        }

        return null
    }
}