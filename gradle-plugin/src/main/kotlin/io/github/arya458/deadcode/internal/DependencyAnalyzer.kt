package io.github.arya458.deadcode.internal

import io.github.arya458.deadcode.core.model.ClassScanModel
import io.github.arya458.deadcode.core.model.DependencyAnalyzerModel
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedArtifact
import java.util.jar.JarFile

class DependencyAnalyzer(private val project: Project) {

    fun analyze(classScan: ClassScanModel): DependencyAnalyzerModel {
        val runtime = runCatching { project.configurations.getByName("runtimeClasspath") }.getOrNull()
        val compile = runCatching { project.configurations.getByName("compileClasspath") }.getOrNull()

        val allArtifacts = buildList {
            runtime?.let { addAll(it.resolvedConfiguration.resolvedArtifacts) }
            compile?.let { addAll(it.resolvedConfiguration.resolvedArtifacts) }
        }.distinct()

        val declaredDeps = runCatching {
            project.configurations.getByName("implementation").dependencies.map { dep ->
                "${dep.group}:${dep.name}"
            }.toSet()
        }.getOrDefault(emptySet())

        val depToClasses = allArtifacts.associate { artifact ->
            val gav = "${artifact.moduleVersion.id.group}:${artifact.moduleVersion.id.name}"
            gav to extractClassesFromJar(artifact)
        }

        val usedDeps = depToClasses.filter { (_, classes) ->
            classes.any { it in classScan.referencedClasses || it.replace('.', '/') in classScan.referencedClasses }
        }.keys

        return DependencyAnalyzerModel(
            declaredDeps = declaredDeps,
            usedDeps = usedDeps,
            deadDeps = declaredDeps - usedDeps
        )
    }

    private fun extractClassesFromJar(artifact: ResolvedArtifact): Set<String> {
        val jarFile = artifact.file
        if (!jarFile.name.endsWith(".jar")) return emptySet()
        return try {
            JarFile(jarFile).use { jar ->
                jar.entries().asSequence()
                    .filter { !it.isDirectory && it.name.endsWith(".class") }
                    .map { it.name.removeSuffix(".class").replace('/', '.') }
                    .toSet()
            }
        } catch (_: Exception) {
            emptySet()
        }
    }
}
