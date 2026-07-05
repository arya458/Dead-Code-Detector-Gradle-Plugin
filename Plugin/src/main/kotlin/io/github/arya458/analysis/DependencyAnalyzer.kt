package io.github.arya458.analysis

import io.github.arya458.model.ClassScanModel
import io.github.arya458.model.DependencyAnalyzerModel
import org.gradle.api.Project
import java.util.jar.JarFile

/**
 * Analyzes project dependencies to find unused ones.
 * Compares declared dependencies against classes referenced in bytecode.
 */
class DependencyAnalyzer(
    private val project: Project
) {

    fun analyze(classScan: ClassScanModel): DependencyAnalyzerModel {
        val runtimeClasspath = project.configurations.getByName("runtimeClasspath")

        // All dependencies (direct + transitive) as GAV strings
        val allArtifacts = runtimeClasspath.resolvedConfiguration.resolvedArtifacts
        val allDeps = allArtifacts.map { artifact ->
            artifact.moduleVersion.id.group + ":" + artifact.moduleVersion.id.name
        }.toSet()

        // Directly declared dependencies (from build.gradle)
        val declaredDeps = project.configurations.getByName("runtimeClasspath").dependencies.map { dep ->
            dep.group + ":" + dep.name
        }.toSet()

        // Map each artifact to the set of fully qualified class names it provides
        val depToClasses = allArtifacts.associate { artifact ->
            val jarFile = artifact.file
            val classes = if (jarFile.name.endsWith(".jar")) {
                JarFile(jarFile).use { jar ->
                    jar.entries().asSequence()
                        .filter { !it.isDirectory && it.name.endsWith(".class") }
                        .map { it.name.removeSuffix(".class").replace('/', '.') }
                        .toSet()
                }
            } else emptySet()

            val gav = artifact.moduleVersion.id.group + ":" + artifact.moduleVersion.id.name
            gav to classes
        }

        // Determine which dependencies have at least one class referenced in bytecode
        val usedDeps = depToClasses.filter { (_, classes) ->
            classes.any { it in classScan.referencedClasses }
        }.keys

        // Unused declared dependencies (direct dependencies that are not used)
        val deadDeclaredDeps = declaredDeps - usedDeps

        return DependencyAnalyzerModel(
            declaredDeps = declaredDeps,
            usedDeps = usedDeps,
            deadDeps = deadDeclaredDeps
        )
    }
}