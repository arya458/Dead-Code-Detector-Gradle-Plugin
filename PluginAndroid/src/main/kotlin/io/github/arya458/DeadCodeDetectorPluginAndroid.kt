package io.github.arya458

import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure // Retaining this if specifically used elsewhere, though often not needed with direct extension access
import org.gradle.kotlin.dsl.register

class DeadCodeDetectorPluginAndroid : Plugin<Project> {
    override fun apply(project: Project) {
        // Register extension so users can configure it in build.gradle.kts
        val extension = project.extensions.create(
            "deadCodeDetector",
            DeadCodeDetectorExtension::class.java
        )

        // Set default conventions for the extension properties
        extension.failOnDeadCode.convention(false)
        extension.includeTests.convention(false)
        extension.keepPublicApi.convention(false)
        extension.includeResources.convention(false)
        // resourceDir and testResourceDir are part of the extension for potential JVM use,
        // but not directly used by configureForAndroid which relies on variant outputs.
        // If they need defaults for the JVM task, they can be added here, e.g.:
        // extension.resourceDir.convention("src/main/resources") 
        // extension.testResourceDir.convention("src/test/resources")
        extension.excludePackages.convention(emptyList())
        extension.keepAnnotations.convention(emptyList())

        // Android (Application or Library) projects
        project.plugins.withId("com.android.application") {
            project.configureForAndroid(extension)
        }
        project.plugins.withId("com.android.library") {
            project.configureForAndroid(extension)
        }

        // Plain JVM projects
        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            project.tasks.register<DeadCodeDetectorTask>("deadCodeDetector") { // This is the single task for JVM projects
                group = "verification"
                description = "Detect dead code in JVM project"

                failOnDeadCode.set(extension.failOnDeadCode)
                includeTests.set(extension.includeTests)
                keepPublicApi.set(extension.keepPublicApi)
                includeResources.set(extension.includeResources)
                excludePackages.set(extension.excludePackages)
                keepAnnotations.set(extension.keepAnnotations)

                // JVM class/resource dirs
                allClasses.from(
                    project.fileTree("build/classes/java/main"),
                    project.fileTree("build/classes/kotlin/main")
                )
                // For JVM projects, if resourceDir extension property is used:
                // allResourceDirs.from(project.file(extension.resourceDir))
                // Or, more directly if the extension property isn't meant for configuration for this specific input:
                allResourceDirs.from(project.file("src/main/resources"))

                reportFile.set(project.layout.buildDirectory.file("reports/dead-code-detector/report.txt"))
            }
        }
    }
}

// --- Android Support (AGP 8+) ---
private fun Project.configureForAndroid(extension: DeadCodeDetectorExtension) {
    val androidComponents = extensions.findByType(AndroidComponentsExtension::class.java)
    androidComponents?.onVariants { variant ->
        val taskName = "deadCodeDetector${variant.name.replaceFirstChar { it.uppercaseChar() }}"
        tasks.register<DeadCodeDetectorTask>(taskName) {
            group = "verification"
            description = "Detect dead code in the ${variant.name} build"

            failOnDeadCode.set(extension.failOnDeadCode)
            includeTests.set(extension.includeTests)
            keepPublicApi.set(extension.keepPublicApi)
            includeResources.set(extension.includeResources)
            excludePackages.set(extension.excludePackages)
            keepAnnotations.set(extension.keepAnnotations)

            allClasses.from(
                layout.buildDirectory.dir("intermediates/javac/${variant.name}/classes"),
                layout.buildDirectory.dir("tmp/kotlin-classes/${variant.name}")
            )

            allResourceDirs.from(
                layout.buildDirectory.dir("intermediates/merged_res/${variant.name}")
            )

            reportFile.set(
                layout.buildDirectory.file("reports/dead-code-detector/${variant.name}.txt")
            )
        }
    }
}
