package com.aria.danesh.report

import com.aria.danesh.DeadCodeDetectorExtension
import com.aria.danesh.model.DeadCodeModel
import com.aria.danesh.model.DependencyAnalyzerModel
import org.gradle.api.logging.Logger
import java.io.File

class ReportWriter(
    private val extension: DeadCodeDetectorExtension,
) {
    private object Ansi {
        const val RESET = "\u001B[0m"
        const val RED = "\u001B[31m"
        const val GREEN = "\u001B[32m"
        const val YELLOW = "\u001B[33m"
        const val CYAN = "\u001B[36m"
        const val BOLD = "\u001B[1m"
    }

    fun write(result: DeadCodeModel, deps: DependencyAnalyzerModel, file: File) {
        val content = buildString {
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

        file.writeText(content)

        println()
        println("${Ansi.BOLD}${Ansi.CYAN}==== Dead Code Detector Summary ====${Ansi.RESET}")
        println("${Ansi.YELLOW} • Dead methods   :${Ansi.RESET} ${result.deadMethods.size}")
        println("${Ansi.YELLOW} • Dead fields    :${Ansi.RESET} ${result.deadFields.size}")
        println("${Ansi.YELLOW} • Dead classes   :${Ansi.RESET} ${result.deadClasses.size}")
        if (extension.includeResources) {
            println("${Ansi.YELLOW} • Dead resources :${Ansi.RESET} ${result.deadResources.size}")
        }
        if (extension.analyzeDependencies) {
            println("${Ansi.YELLOW} • Unused deps    :${Ansi.RESET} ${deps.deadDeps.size}")
        }

        if (result.hasDeadCode() || (extension.analyzeDependencies && deps.hasUnusedDependencies())) {
            println("${Ansi.RED}⚠ Dead code, resources, or unused dependencies found.${Ansi.RESET}")
        } else {
            println("${Ansi.GREEN}✔ No dead code, resources, or unused dependencies detected!${Ansi.RESET}")
        }

        println("${Ansi.BOLD}${Ansi.CYAN}==== Report saved at ====${Ansi.RESET}")
        println(file.absolutePath)
        println()
    }
}
