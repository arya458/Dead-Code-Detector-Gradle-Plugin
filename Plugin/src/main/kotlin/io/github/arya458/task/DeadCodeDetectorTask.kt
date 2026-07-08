package io.github.arya458.task

import io.github.arya458.*
import io.github.arya458.analysis.*
import io.github.arya458.model.DependencyAnalyzerModel
import io.github.arya458.report.ReportWriter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
open class DeadCodeDetectorTask : DefaultTask() {

    @get:Input
    lateinit var extension: DeadCodeDetectorExtension

    @TaskAction
    fun runDetector() {
        val classesRoot = project.layout.buildDirectory.dir("classes").get().asFile
        val mainResRoot = project.projectDir.resolve(extension.resourceDir)
        val testResRoot = project.projectDir.resolve(extension.testResourceDir)

        // Collect configuration directories for Spring
        val configRoots = extension.configDirs.map { project.projectDir.resolve(it) }

        if (!classesRoot.exists()) {
            logger.lifecycle("No compiled classes found in $classesRoot -> run compile first.")
            return
        }

        // 1. Scan classes
        val classScanner = ClassScanner(parallel = extension.parallelScan)
        val classScan = classScanner.scan(classesRoot, extension.includeTests)

        // 2. Scan resources (Android + Spring configs)
        val resScan = ResourceScanner(extension).scan(mainResRoot, testResRoot, classesRoot, configRoots)

        // 3. Analyze dead code
        val analysis = DeadCodeAnalyzer(extension).analyze(classScan, resScan)

        // 4. Dependency analysis
        val depAnalysis = if (extension.analyzeDependencies) {
            DependencyAnalyzer(project).analyze(classScan)
        } else {
            DependencyAnalyzerModel(emptySet(), emptySet(), emptySet())
        }

        // 5. Generate reports
        val reportDir = project.layout.buildDirectory.dir("reports/dead-code-detector").get().asFile.also { it.mkdirs() }
        val reportFile = reportDir.resolve("report-${project.name}.txt")

        ReportWriter(extension).write(analysis, depAnalysis, reportFile)

        // 6. Fail build if configured
        val shouldFail = (extension.failOnDeadCode && analysis.hasDeadCode()) ||
                (extension.failOnUnusedDependencies && depAnalysis.hasUnusedDependencies())

        if (shouldFail) {
            throw RuntimeException("\nDead code/resources or unused dependencies detected. See\n ${reportFile.absolutePath}\n")
        }
    }
}