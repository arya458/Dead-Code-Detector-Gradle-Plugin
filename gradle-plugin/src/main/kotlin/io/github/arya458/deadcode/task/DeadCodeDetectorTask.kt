package io.github.arya458.deadcode.task

import io.github.arya458.deadcode.DeadCodeDetectorExtension
import io.github.arya458.deadcode.core.analyzer.AnalyzerConfig
import io.github.arya458.deadcode.core.analyzer.DeadCodeAnalyzer
import io.github.arya458.deadcode.core.model.DependencyAnalyzerModel
import io.github.arya458.deadcode.core.scanner.BytecodeClassScanner
import io.github.arya458.deadcode.internal.ClassDirectoryResolver
import io.github.arya458.deadcode.internal.DependencyAnalyzer
import io.github.arya458.deadcode.internal.PlatformResolver
import io.github.arya458.deadcode.internal.ResourceScanner
import io.github.arya458.deadcode.report.ReportWriter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class DeadCodeDetectorTask : DefaultTask() {

    @get:Input
    lateinit var extension: DeadCodeDetectorExtension

    @TaskAction
    fun runDetector() {
        val classDirs = ClassDirectoryResolver.resolve(project, extension.includeTests)
        val classScan = BytecodeClassScanner(parallel = extension.parallelScan)
            .scan(classDirs, extension.includeOnlyPackages)

        val resScan = ResourceScanner(project, extension).scan()

        val platformRules = PlatformResolver.resolve(project, extension.platform)
        val analyzer = DeadCodeAnalyzer(
            platformRules = platformRules,
            config = AnalyzerConfig(
                keepPublicApi = extension.keepPublicApi,
                includeResources = extension.includeResources,
                keepAnnotations = extension.keepAnnotations.toSet(),
                excludePackages = extension.excludePackages.toList(),
                excludeClasses = extension.excludeClasses.toList(),
                excludeMethodNames = extension.excludeMethods.map { it.pattern().toRegex() },
                excludeFieldNames = extension.excludeFields.map { it.pattern().toRegex() }
            )
        )
        val analysis = analyzer.analyze(classScan, resScan)

        val depAnalysis = if (extension.analyzeDependencies) {
            DependencyAnalyzer(project).analyze(classScan)
        } else {
            DependencyAnalyzerModel()
        }

        val reportDir = project.layout.buildDirectory.dir("reports/dead-code-detector").get().asFile
        reportDir.mkdirs()
        val reportFile = reportDir.resolve("report-${project.name}.txt")
        ReportWriter(extension).write(analysis, depAnalysis, reportFile)

        val shouldFail =
            (extension.failOnDeadCode && analysis.hasDeadCode()) ||
                (extension.failOnUnusedDependencies && depAnalysis.hasUnusedDependencies())

        if (shouldFail) {
            throw RuntimeException(
                "Dead code or unused dependencies detected. See ${reportFile.absolutePath}"
            )
        }
    }
}
