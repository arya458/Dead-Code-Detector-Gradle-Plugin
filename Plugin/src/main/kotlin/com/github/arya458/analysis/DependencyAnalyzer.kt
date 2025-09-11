package com.github.arya458.analysis

import com.github.arya458.model.ClassScanModel
import com.github.arya458.model.DependencyAnalyzerModel

import org.gradle.api.Project
import java.util.jar.JarFile

class DependencyAnalyzer(
    private val project: Project
) {

    fun analyze(classScan: ClassScanModel): DependencyAnalyzerModel {
        val runtimeClasspath = project.configurations.getByName("runtimeClasspath")

        // Collect declared dependencies (group:name)
        val declaredDeps = runtimeClasspath.resolvedConfiguration.resolvedArtifacts.map { artifact ->
            artifact.moduleVersion.id.group + ":" + artifact.moduleVersion.id.name
        }.toSet()

        // Map each dependency → set of provided classes
        val depToClasses = runtimeClasspath.resolvedConfiguration.resolvedArtifacts.associate { artifact ->
            val jarFile = artifact.file
            val classes = if (jarFile.name.endsWith(".jar")) {
                JarFile(jarFile).use { jar ->
                    jar.entries().asSequence()
                        .filter { !it.isDirectory && it.name.endsWith(".class") }
                        .map { it.name.removeSuffix(".class") }
                        .toSet()
                }
            } else emptySet()

            val gav = artifact.moduleVersion.id.group + ":" + artifact.moduleVersion.id.name
            gav to classes
        }

        // Check usage: if any class from dep is referenced in bytecode
        val usedDeps = depToClasses.filter { (_, classes) ->
            classes.any { it in classScan.referencedClasses }
        }.keys

        val deadDeps = declaredDeps - usedDeps

        return DependencyAnalyzerModel(declaredDeps, usedDeps, deadDeps)
    }
}