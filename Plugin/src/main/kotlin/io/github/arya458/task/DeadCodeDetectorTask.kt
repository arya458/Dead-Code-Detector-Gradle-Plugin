package io.github.arya458.task

import io.github.arya458.DeadCodeDetectorExtension
import io.github.arya458.analysis.ClassScanner
import io.github.arya458.analysis.DeadCodeAnalyzer
import io.github.arya458.analysis.DependencyAnalyzer
import io.github.arya458.analysis.ResourceScanner
import io.github.arya458.model.DependencyAnalyzerModel
import io.github.arya458.report.ReportWriter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class DeadCodeDetectorTask : DefaultTask() {

    @get:Input
    lateinit var extension: DeadCodeDetectorExtension

    @TaskAction
    fun runDetector() {
        // 1. Scan classes با کش و پشتیبانی از چندماژول و includeOnlyPackages
        val classScanner = ClassScanner(
            project,
            parallel = extension.parallelScan,
            enableCaching = extension.enableCaching
        )
        val classScan = classScanner.scan(
            includeTests = extension.includeTests,
            includeOnlyPackages = extension.includeOnlyPackages
        )

        // 2. Scan resources
        val resScanner = ResourceScanner(project, extension)
        val resScan = resScanner.scan()

        // 3. Analyze dead code
        val analyzer = DeadCodeAnalyzer(project, extension)
        val analysis = analyzer.analyze(classScan, resScan)

        // 4. Analyze dependencies
        val depAnalysis = if (extension.analyzeDependencies) {
            DependencyAnalyzer(project).analyze(classScan)
        } else {
            DependencyAnalyzerModel(emptySet(), emptySet(), emptySet())
        }

        // 5. Generate reports
        val reportDir = project.layout.buildDirectory.dir("reports/dead-code-detector").get().asFile
        reportDir.mkdirs()
        val reportFile = reportDir.resolve("report-${project.name}.txt")

        ReportWriter(extension).write(analysis, depAnalysis, reportFile)

        // 6. Fail build if configured
        val shouldFail = (extension.failOnDeadCode && analysis.hasDeadCode()) ||
                (extension.failOnUnusedDependencies && depAnalysis.hasUnusedDependencies())

        if (shouldFail) {
            throw RuntimeException("Dead code/resources or unused dependencies detected. See ${reportFile.absolutePath}")
        }
    }
}