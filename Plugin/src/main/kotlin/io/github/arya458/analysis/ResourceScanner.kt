package io.github.arya458.analysis

import io.github.arya458.DeadCodeDetectorExtension
import io.github.arya458.model.ResourceModel
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet

class ResourceScanner(
    private val project: Project,
    private val extension: DeadCodeDetectorExtension
) {

    fun scan(): ResourceModel {
        val declared = ConcurrentSkipListSet<Pair<String, String>>()
        val referenced = ConcurrentSkipListSet<Pair<String, String>>()
        val referencedClassesFromManifest = ConcurrentSkipListSet<String>()
        val referencedClassesFromSpring = ConcurrentSkipListSet<String>()

        val mainResRoot = project.projectDir.resolve(extension.resourceDir)
        val testResRoot = project.projectDir.resolve(extension.testResourceDir)

        scanResources(mainResRoot, declared, referenced)
        if (extension.includeTests) {
            scanResources(testResRoot, declared, referenced)
        }

        // Scan R.class
        val sourceSets = project.extensions.findByType(SourceSetContainer::class.java)
        if (sourceSets != null) {
            sourceSets.getByName("main").output.classesDirs.files.forEach { dir ->
                scanRClass(dir, declared)
            }
        } else {
            val fallbackDir = project.layout.buildDirectory.dir("classes/java/main").get().asFile
            scanRClass(fallbackDir, declared)
        }

        val manifestFile = File(mainResRoot.parentFile, "AndroidManifest.xml")
        scanManifest(manifestFile, referencedClassesFromManifest)

        if (extension.scanConfigFiles) {
            extension.configDirs.forEach { configDir ->
                val dir = project.projectDir.resolve(configDir)
                scanSpringConfigs(dir, referencedClassesFromSpring)
            }
        }

        return ResourceModel(
            declared = declared.toSet(),
            referenced = referenced.toSet(),
            referencedClassesFromManifest = referencedClassesFromManifest.toSet(),
            referencedClassesFromSpringConfig = referencedClassesFromSpring.toSet()
        )
    }

    private fun scanResources(resRoot: File, declared: MutableSet<Pair<String, String>>, referenced: MutableSet<Pair<String, String>>) {
        if (!extension.includeResources || !resRoot.exists()) return

        resRoot.walkTopDown().forEach { file ->
            if (!file.isFile) return@forEach
            val type = file.parentFile.name
            val name = file.nameWithoutExtension
            when (type) {
                "layout", "drawable", "mipmap", "menu", "raw" -> declared.add(type to name)
                "values" -> {
                    if (file.extension == "xml") {
                        val xml = file.readText()
                        Regex("<(string|color|dimen|style|array) name=\"(.*?)\"").findAll(xml).forEach {
                            declared.add(it.groupValues[1] to it.groupValues[2])
                        }
                    }
                }
                else -> {
                    if (file.extension in listOf("xml", "png", "jpg", "webp", "svg")) {
                        declared.add(type to name)
                    }
                }
            }
        }

        resRoot.walkTopDown().filter { it.extension == "xml" }.forEach { file ->
            val xml = file.readText()
            Regex("@(\\w+)/(\\w+)").findAll(xml).forEach {
                referenced.add(it.groupValues[1] to it.groupValues[2])
            }
            Regex("\\?attr/(\\w+)").findAll(xml).forEach {
                referenced.add("attr" to it.groupValues[1])
            }
        }
    }

    private fun scanRClass(rDir: File, declared: MutableSet<Pair<String, String>>) {
        val rFile = rDir.walkTopDown().find { it.name == "R.class" } ?: return
        val rParent = rFile.parentFile
        rParent.listFiles { f -> f.isDirectory && f.name.startsWith("R$") }?.forEach { innerDir ->
            val type = innerDir.name.substringAfter("R$")
            innerDir.walkTopDown().filter { it.isFile && it.name.endsWith(".class") }.forEach { classFile ->
                try {
                    val cr = ClassReader(classFile.readBytes())
                    cr.accept(object : ClassVisitor(Opcodes.ASM9) {
                        override fun visitField(access: Int, name: String, descriptor: String, sig: String?, value: Any?): FieldVisitor? {
                            if (descriptor == "I") {
                                declared.add(type to name)
                            }
                            return super.visitField(access, name, descriptor, sig, value)
                        }
                    }, ClassReader.SKIP_FRAMES)
                } catch (e: Exception) {
                    project.logger.debug("Could not scan R.class in ${classFile.absolutePath}: ${e.message}")
                }
            }
        }
    }

    private fun scanManifest(manifestFile: File?, referencedClasses: MutableSet<String>) {
        if (manifestFile == null || !manifestFile.exists()) return
        val xml = manifestFile.readText()
        Regex("android:name=\"([^\"]+)\"").findAll(xml).forEach {
            val name = it.groupValues[1]
            if (!name.startsWith(".")) {
                referencedClasses.add(name.replace('.', '/'))
            }
        }
    }

    private fun scanSpringConfigs(dir: File, referencedClasses: MutableSet<String>) {
        if (!dir.exists()) return
        dir.walkTopDown().forEach { file ->
            when {
                file.name.endsWith(".xml") -> {
                    val xml = file.readText()
                    Regex("class=\"([^\"]+)\"").findAll(xml).forEach {
                        val cls = it.groupValues[1]
                        referencedClasses.add(cls.replace('.', '/'))
                    }
                }
                file.name.matches(Regex("application.*\\.(yml|yaml|properties)")) -> {
                    val content = file.readText()
                    Regex("[a-zA-Z_][\\w.]*\\.[a-zA-Z_][\\w.]*").findAll(content).forEach {
                        val cls = it.value
                        if (cls.matches(Regex("^[a-z]+\\.[a-zA-Z_][\\w.]*$"))) {
                            referencedClasses.add(cls.replace('.', '/'))
                        }
                    }
                }
            }
        }
    }
}