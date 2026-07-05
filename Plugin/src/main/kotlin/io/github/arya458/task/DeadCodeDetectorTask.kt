package io.github.arya458.task

import io.github.arya458.*
import io.github.arya458.analysis.*
import io.github.arya458.model.DependencyAnalyzerModel
import io.github.arya458.report.ReportWriter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Gradle task that runs the dead code detection.
 * It is cacheable and depends on the 'classes' task.
 */
@CacheableTask
open class DeadCodeDetectorTask : DefaultTask() {

    @get:Input
    lateinit var extension: DeadCodeDetectorExtension

    @TaskAction
    fun runDetector() {
        val classesRoot = project.layout.buildDirectory.dir("classes").get().asFile
        val mainResRoot = project.projectDir.resolve(extension.resourceDir)
        val testResRoot = project.projectDir.resolve(extension.testResourceDir)

        if (!classesRoot.exists()) {
            logger.lifecycle("No compiled classes found in $classesRoot -> run compile first.")
            return
        }

        // Step 1: Scan classes
        val classScan = ClassScanner().scan(classesRoot, extension.includeTests)

        // Step 2: Scan resources (includes R.class and manifest)
        val resScan = ResourceScanner(extension).scan(mainResRoot, testResRoot, classesRoot)

        // Step 3: Analyze dead code
        val analysis = DeadCodeAnalyzer(extension).analyze(classScan, resScan)

        // Step 4: Analyze dependencies if enabled
        val depAnalysis = if (extension.analyzeDependencies) {
            DependencyAnalyzer(project).analyze(classScan)
        } else {
            DependencyAnalyzerModel(emptySet(), emptySet(), emptySet())
        }

        // Step 5: Generate reports
        val reportDir = project.layout.buildDirectory.dir("reports/dead-code-detector").get().asFile.also { it.mkdirs() }
        val reportFile = reportDir.resolve("report-${project.name}.txt")

        ReportWriter(extension).write(analysis, depAnalysis, reportFile)

        // Step 6: Fail build if configured
        val shouldFail = (extension.failOnDeadCode && analysis.hasDeadCode()) ||
                (extension.failOnUnusedDependencies && depAnalysis.hasUnusedDependencies())

        if (shouldFail) {
            throw RuntimeException("Dead code/resources or unused dependencies detected. See ${reportFile.absolutePath}")
        }
    }
}