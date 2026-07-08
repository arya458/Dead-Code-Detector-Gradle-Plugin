package io.github.arya458.analysis

import io.github.arya458.model.ClassScanModel
import io.github.arya458.model.DependencyAnalyzerModel
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedArtifact
import java.util.jar.JarFile

class DependencyAnalyzer(
    private val project: Project
) {

    fun analyze(classScan: ClassScanModel): DependencyAnalyzerModel {
        val runtimeClasspath = project.configurations.getByName("runtimeClasspath")
        val compileClasspath = project.configurations.getByName("compileClasspath")

        // ترکیب هر دو برای پوشش کامل‌تر
        val allArtifacts = (runtimeClasspath.resolvedConfiguration.resolvedArtifacts +
                compileClasspath.resolvedConfiguration.resolvedArtifacts).distinct()

        // وابستگی‌های مستقیم (declared)
        val declaredDeps = project.configurations.getByName("implementation").dependencies.map { dep ->
            dep.group + ":" + dep.name
        }.toSet()

        // نقشه‌ی وابستگی به کلاس‌ها (با کش کردن)
        val depToClasses = allArtifacts.associate { artifact ->
            val gav = artifact.moduleVersion.id.group + ":" + artifact.moduleVersion.id.name
            gav to extractClassesFromJar(artifact)
        }

        // تشخیص وابستگی‌های استفاده‌شده
        val usedDeps = depToClasses.filter { (_, classes) ->
            classes.any { it in classScan.referencedClasses }
        }.keys

        val deadDeclaredDeps = declaredDeps - usedDeps

        return DependencyAnalyzerModel(
            declaredDeps = declaredDeps,
            usedDeps = usedDeps,
            deadDeps = deadDeclaredDeps
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
        } catch (e: Exception) {
            project.logger.debug("Could not read jar ${jarFile.name}: ${e.message}")
            emptySet()
        }
    }
}