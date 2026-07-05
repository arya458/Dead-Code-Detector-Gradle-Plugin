package io.github.arya458.report

import io.github.arya458.DeadCodeDetectorExtension
import io.github.arya458.model.DependencyAnalyzerModel
import java.io.File

/**
 * Writes analysis results in multiple formats: plain text, HTML, and JSON.
 * Also prints a summary to the console.
 */
class ReportWriter(
    private val extension: DeadCodeDetectorExtension,
) {

    fun write(result: io.github.arya458.model.DeadCodeModel, deps: DependencyAnalyzerModel, file: File) {
        // Text report
        val textContent = buildTextReport(result, deps)
        file.writeText(textContent)

        // HTML report
        val htmlFile = File(file.parent, file.nameWithoutExtension + ".html")
        htmlFile.writeText(buildHtmlReport(result, deps))

        // JSON report
        val jsonFile = File(file.parent, file.nameWithoutExtension + ".json")
        jsonFile.writeText(buildJsonReport(result, deps))

        // Console summary
        printSummary(result, deps)
    }

    private fun buildTextReport(result: io.github.arya458.model.DeadCodeModel, deps: DependencyAnalyzerModel): String {
        return buildString {
            appendLine("╔══════════════════════════════════════════════════════════════════════════════════════════╗")
            appendLine("║                            Dead Code Detector Report                                     ║")
            appendLine("╚══════════════════════════════════════════════════════════════════════════════════════════╝")
            appendLine()
            appendLine("👤 Developed by Aria Danesh")
            appendLine("📦 GitHub : https://github.com/arya458/Dead-Code-Detector-Gradle-Plugin")
            appendLine("✉️  Email  : aria.danesh.work@gmail.com")
            appendLine()
            appendLine("─────────────────────────────── Summary ───────────────────────────────")
            appendLine(" • Dead methods    : ${result.deadMethods.size}")
            appendLine(" • Dead fields     : ${result.deadFields.size}")
            appendLine(" • Dead classes    : ${result.deadClasses.size}")
            if (extension.includeResources) appendLine(" • Dead resources  : ${result.deadResources.size}")
            if (extension.analyzeDependencies) appendLine(" • Unused deps     : ${deps.deadDeps.size}")
            appendLine("────────────────────────────────────────────────────────────────────────")
            appendLine()

            if (result.deadClasses.isNotEmpty()) {
                appendLine("📂 Dead Classes:")
                result.deadClasses.forEach { appendLine("   - $it") }
                appendLine()
            }

            if (result.deadMethods.isNotEmpty()) {
                appendLine("🔧 Dead Methods:")
                result.deadMethods.groupBy { it.owner.replace('/', '.') }.forEach { (klass, methods) ->
                    appendLine("   Class: $klass")
                    methods.forEach { m -> appendLine("     • ${m.name}${m.desc}") }
                }
                appendLine()
            }

            if (result.deadFields.isNotEmpty()) {
                appendLine("🏷 Dead Fields:")
                result.deadFields.groupBy { it.owner.replace('/', '.') }.forEach { (klass, fields) ->
                    appendLine("   Class: $klass")
                    fields.forEach { f -> appendLine("     • ${f.name} : ${f.desc}") }
                }
                appendLine()
            }

            if (extension.includeResources && result.deadResources.isNotEmpty()) {
                appendLine("📦 Dead Resources:")
                result.deadResources.groupBy { it.first }.forEach { (type, resources) ->
                    appendLine("   Type: $type")
                    resources.forEach { (_, name) -> appendLine("     • $name") }
                }
                appendLine()
            }

            if (extension.analyzeDependencies && deps.deadDeps.isNotEmpty()) {
                appendLine("📦 Unused Dependencies:")
                deps.deadDeps.forEach { dep -> appendLine("   - $dep") }
                appendLine()
            }
        }
    }

    private fun buildHtmlReport(result: io.github.arya458.model.DeadCodeModel, deps: DependencyAnalyzerModel): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Dead Code Detector Report</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; background: #f8f9fa; }
                    .container { max-width: 1200px; margin: auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    h1 { color: #2c3e50; }
                    .summary { background: #e9ecef; padding: 15px; border-radius: 5px; margin-bottom: 20px; }
                    .summary p { margin: 5px 0; }
                    .badge { display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 14px; }
                    .badge-danger { background: #dc3545; color: white; }
                    .badge-success { background: #28a745; color: white; }
                    .section { margin-top: 30px; }
                    .section h2 { border-bottom: 2px solid #dee2e6; padding-bottom: 5px; }
                    .item { margin-left: 20px; }
                    .klass { font-weight: bold; color: #495057; margin-top: 10px; }
                    .member { margin-left: 30px; color: #6c757d; }
                </style>
            </head>
            <body>
            <div class="container">
                <h1>Dead Code Detector Report</h1>
                <div class="summary">
                    <p><strong>Dead Methods:</strong> ${result.deadMethods.size}</p>
                    <p><strong>Dead Fields:</strong> ${result.deadFields.size}</p>
                    <p><strong>Dead Classes:</strong> ${result.deadClasses.size}</p>
                    ${if (extension.includeResources) "<p><strong>Dead Resources:</strong> ${result.deadResources.size}</p>" else ""}
                    ${if (extension.analyzeDependencies) "<p><strong>Unused Dependencies:</strong> ${deps.deadDeps.size}</p>" else ""}
                </div>

                <div class="section">
                    <h2>Dead Classes</h2>
                    ${if (result.deadClasses.isEmpty()) "<p>No dead classes found.</p>" else
            "<ul>${result.deadClasses.map { "<li>$it</li>" }.joinToString("")}</ul>"}
                </div>

                <div class="section">
                    <h2>Dead Methods</h2>
                    ${if (result.deadMethods.isEmpty()) "<p>No dead methods found.</p>" else
            result.deadMethods.groupBy { it.owner }.map { (owner, methods) ->
                "<div class='klass'>$owner</div><ul>${methods.map { "<li class='member'>${it.name}${it.desc}</li>" }.joinToString("")}</ul>"
            }.joinToString("")}
                </div>

                <div class="section">
                    <h2>Dead Fields</h2>
                    ${if (result.deadFields.isEmpty()) "<p>No dead fields found.</p>" else
            result.deadFields.groupBy { it.owner }.map { (owner, fields) ->
                "<div class='klass'>$owner</div><ul>${fields.map { "<li class='member'>${it.name} : ${it.desc}</li>" }.joinToString("")}</ul>"
            }.joinToString("")}
                </div>

                ${if (extension.includeResources && result.deadResources.isNotEmpty()) """
                <div class="section">
                    <h2>Dead Resources</h2>
                    ${result.deadResources.groupBy { it.first }.map { (type, resources) ->
            "<div class='klass'>$type</div><ul>${resources.map { "<li class='member'>${it.second}</li>" }.joinToString("")}</ul>"
        }.joinToString("")}
                </div>
                """ else ""}

                ${if (extension.analyzeDependencies && deps.deadDeps.isNotEmpty()) """
                <div class="section">
                    <h2>Unused Dependencies</h2>
                    <ul>${deps.deadDeps.map { "<li>$it</li>" }.joinToString("")}</ul>
                </div>
                """ else ""}
            </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildJsonReport(result: io.github.arya458.model.DeadCodeModel, deps: DependencyAnalyzerModel): String {
        return """
            {
                "deadMethods": ${result.deadMethods.map { mapOf("owner" to it.owner, "name" to it.name, "desc" to it.desc) }},
                "deadFields": ${result.deadFields.map { mapOf("owner" to it.owner, "name" to it.name, "desc" to it.desc) }},
                "deadClasses": ${result.deadClasses},
                "deadResources": ${result.deadResources.map { mapOf("type" to it.first, "name" to it.second) }},
                "unusedDependencies": ${deps.deadDeps}
            }
        """.trimIndent()
    }

    private fun printSummary(result: io.github.arya458.model.DeadCodeModel, deps: DependencyAnalyzerModel) {
        val ansi = object {
            val RESET = "\u001B[0m"
            val RED = "\u001B[31m"
            val GREEN = "\u001B[32m"
            val YELLOW = "\u001B[33m"
            val CYAN = "\u001B[36m"
            val BOLD = "\u001B[1m"
        }

        println()
        println("${ansi.BOLD}${ansi.CYAN}==== Dead Code Detector Summary ====${ansi.RESET}")
        println("${ansi.YELLOW} • Dead methods   :${ansi.RESET} ${result.deadMethods.size}")
        println("${ansi.YELLOW} • Dead fields    :${ansi.RESET} ${result.deadFields.size}")
        println("${ansi.YELLOW} • Dead classes   :${ansi.RESET} ${result.deadClasses.size}")
        if (extension.includeResources) {
            println("${ansi.YELLOW} • Dead resources :${ansi.RESET} ${result.deadResources.size}")
        }
        if (extension.analyzeDependencies) {
            println("${ansi.YELLOW} • Unused deps    :${ansi.RESET} ${deps.deadDeps.size}")
        }

        if (result.hasDeadCode() || (extension.analyzeDependencies && deps.hasUnusedDependencies())) {
            println("${ansi.RED}⚠ Dead code, resources, or unused dependencies found.${ansi.RESET}")
        } else {
            println("${ansi.GREEN}✔ No dead code, resources, or unused dependencies detected!${ansi.RESET}")
        }

        println("${ansi.BOLD}${ansi.CYAN}==== Report saved at ====${ansi.RESET}")
    }
}