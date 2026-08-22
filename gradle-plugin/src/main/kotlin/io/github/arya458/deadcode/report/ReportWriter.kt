package io.github.arya458.deadcode.report

import io.github.arya458.deadcode.DeadCodeDetectorExtension
import io.github.arya458.deadcode.core.model.DeadCodeModel
import io.github.arya458.deadcode.core.model.DependencyAnalyzerModel
import java.io.File
import java.nio.charset.StandardCharsets

class ReportWriter(private val extension: DeadCodeDetectorExtension) {

    fun write(result: DeadCodeModel, deps: DependencyAnalyzerModel, file: File) {
        file.writeText(buildTextReport(result, deps), StandardCharsets.UTF_8)
        File(file.parent, file.nameWithoutExtension + ".html")
            .writeText(buildHtmlReport(result, deps), StandardCharsets.UTF_8)
        File(file.parent, file.nameWithoutExtension + ".json")
            .writeText(buildJsonReport(result, deps), StandardCharsets.UTF_8)
        printSummary(result, deps, file.parent ?: ".")
    }

    private fun buildTextReport(result: DeadCodeModel, deps: DependencyAnalyzerModel): String = buildString {
        appendLine("==== Dead Code Detector Report ====")
        appendLine("Dead methods    : ${result.deadMethods.size}")
        appendLine("Dead fields     : ${result.deadFields.size}")
        appendLine("Dead classes    : ${result.deadClasses.size}")
        if (extension.includeResources) appendLine("Dead resources  : ${result.deadResources.size}")
        if (extension.analyzeDependencies) appendLine("Unused deps     : ${deps.deadDeps.size}")
        appendLine()
        if (result.deadClasses.isNotEmpty()) {
            appendLine("Dead Classes:")
            result.deadClasses.forEach { appendLine("  - $it") }
            appendLine()
        }
        if (result.deadMethods.isNotEmpty()) {
            appendLine("Dead Methods:")
            result.deadMethods.forEach { m ->
                appendLine("  - ${m.owner.replace('/', '.')}.${m.name}${m.desc}")
            }
            appendLine()
        }
        if (result.deadFields.isNotEmpty()) {
            appendLine("Dead Fields:")
            result.deadFields.forEach { f ->
                appendLine("  - ${f.owner.replace('/', '.')}.${f.name} : ${f.desc}")
            }
            appendLine()
        }
        if (extension.includeResources && result.deadResources.isNotEmpty()) {
            appendLine("Dead Resources:")
            result.deadResources.forEach { (type, name) -> appendLine("  - $type/$name") }
            appendLine()
        }
        if (extension.analyzeDependencies && deps.deadDeps.isNotEmpty()) {
            appendLine("Unused Dependencies:")
            deps.deadDeps.forEach { appendLine("  - $it") }
        }
    }

    private fun buildHtmlReport(result: DeadCodeModel, deps: DependencyAnalyzerModel): String {
        val classes = result.deadClasses.joinToString("<li>", "<ul><li>", "</li></ul>") { it }
            .ifEmpty { "<p>None</p>" }
        return """
        <!DOCTYPE html><html><head><meta charset="UTF-8"><title>Dead Code Report</title></head>
        <body>
        <h1>Dead Code Detector Report</h1>
        <ul>
          <li>Dead classes: ${result.deadClasses.size}</li>
          <li>Dead methods: ${result.deadMethods.size}</li>
          <li>Dead fields: ${result.deadFields.size}</li>
          <li>Dead resources: ${result.deadResources.size}</li>
          <li>Unused deps: ${deps.deadDeps.size}</li>
        </ul>
        <h2>Classes</h2>$classes
        </body></html>
        """.trimIndent()
    }

    private fun buildJsonReport(result: DeadCodeModel, deps: DependencyAnalyzerModel): String {
        fun esc(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"")
        val classes = result.deadClasses.joinToString(",") { "\"${esc(it)}\"" }
        val methods = result.deadMethods.joinToString(",") {
            """{"owner":"${esc(it.owner)}","name":"${esc(it.name)}","desc":"${esc(it.desc)}"}"""
        }
        val fields = result.deadFields.joinToString(",") {
            """{"owner":"${esc(it.owner)}","name":"${esc(it.name)}","desc":"${esc(it.desc)}"}"""
        }
        val resources = result.deadResources.joinToString(",") {
            """{"type":"${esc(it.first)}","name":"${esc(it.second)}"}"""
        }
        val deadDeps = deps.deadDeps.joinToString(",") { "\"${esc(it)}\"" }
        return """
        {
          "deadClasses": [$classes],
          "deadMethods": [$methods],
          "deadFields": [$fields],
          "deadResources": [$resources],
          "unusedDependencies": [$deadDeps]
        }
        """.trimIndent()
    }

    private fun printSummary(result: DeadCodeModel, deps: DependencyAnalyzerModel, path: String) {
        println("==== Dead Code Detector Summary ====")
        println(" • Dead methods   : ${result.deadMethods.size}")
        println(" • Dead fields    : ${result.deadFields.size}")
        println(" • Dead classes   : ${result.deadClasses.size}")
        if (extension.includeResources) println(" • Dead resources : ${result.deadResources.size}")
        if (extension.analyzeDependencies) println(" • Unused deps    : ${deps.deadDeps.size}")
        if (result.hasDeadCode() || (extension.analyzeDependencies && deps.hasUnusedDependencies())) {
            println("⚠ Dead code or unused dependencies found.")
        } else {
            println("✔ No dead code detected.")
        }
        println("Report: $path")
    }
}
