package io.github.arya458.deadcode.task

import io.github.arya458.deadcode.DeadCodeDetectorExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Thin orchestrator task.
 * Real analysis will be delegated to core + platform modules.
 * Currently a placeholder while migration from legacy Plugin/ is in progress.
 */
open class DeadCodeDetectorTask : DefaultTask() {

    @get:Input
    lateinit var extension: DeadCodeDetectorExtension

    @TaskAction
    fun runDetector() {
        logger.lifecycle("==== Dead Code Detector (modular – early branch) ====")
        logger.lifecycle("Platform hint : ${extension.platform}")
        logger.lifecycle("Core + platforms modules are wired. Full analyzer migration coming next.")
        logger.lifecycle("Legacy Plugin/ module is still available via includeBuild for compatibility.")
    }
}
