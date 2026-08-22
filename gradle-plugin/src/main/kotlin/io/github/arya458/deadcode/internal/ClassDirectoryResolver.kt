package io.github.arya458.deadcode.internal

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import java.io.File

object ClassDirectoryResolver {

    fun resolve(project: Project, includeTests: Boolean): List<File> {
        val dirs = mutableSetOf<File>()
        val sourceSets = project.extensions.findByType(SourceSetContainer::class.java)

        if (sourceSets != null) {
            dirs.addAll(sourceSets.getByName("main").output.classesDirs.files)
            if (includeTests) {
                runCatching {
                    dirs.addAll(sourceSets.getByName("test").output.classesDirs.files)
                }
            }
            project.rootProject.subprojects.forEach { sub ->
                val subSs = sub.extensions.findByType(SourceSetContainer::class.java)
                if (subSs != null) {
                    dirs.addAll(subSs.getByName("main").output.classesDirs.files)
                    if (includeTests) {
                        runCatching {
                            dirs.addAll(subSs.getByName("test").output.classesDirs.files)
                        }
                    }
                }
            }
        } else {
            dirs.add(project.layout.buildDirectory.dir("classes/java/main").get().asFile)
            dirs.add(project.layout.buildDirectory.dir("classes/kotlin/main").get().asFile)
            if (includeTests) {
                dirs.add(project.layout.buildDirectory.dir("classes/java/test").get().asFile)
                dirs.add(project.layout.buildDirectory.dir("classes/kotlin/test").get().asFile)
            }
        }

        return dirs.filter { it.exists() }
    }
}
