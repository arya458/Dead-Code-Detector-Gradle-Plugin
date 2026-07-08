package io.github.arya458.analysis

import io.github.arya458.DeadCodeDetectorExtension
import io.github.arya458.model.ResourceModel
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet

class ResourceScanner(private val extension: DeadCodeDetectorExtension) {

    fun scan(mainResRoot: File, testResRoot: File, classesRoot: File, configRoots: List<File>): ResourceModel {
        val declared = ConcurrentSkipListSet<Pair<String, String>>()
        val referenced = ConcurrentSkipListSet<Pair<String, String>>()
        val referencedClassesFromManifest = ConcurrentSkipListSet<String>()
        val referencedClassesFromSpring = ConcurrentSkipListSet<String>()

        // ---- Android resources ----
        fun scanResources(resRoot: File) {
            if (!extension.includeResources || !resRoot.exists()) return
            resRoot.walkTopDown().forEach { file ->
                if (!file.isFile) return@forEach
                val type = file.parentFile.name
                val name = file.nameWithoutExtension
                when (type) {
                    "layout", "drawable", "mipmap", "menu", "raw" -> declared += type to name
                    "values" -> if (file.extension == "xml") {
                        val xml = file.readText()
                        Regex("<(string|color|dimen|style|array) name=\"(.*?)\"").findAll(xml).forEach {
                            declared += it.groupValues[1] to it.groupValues[2]
                        }
                    }
                    else -> if (file.extension in listOf("xml", "png", "jpg", "webp")) {
                        declared += type to name
                    }
                }
            }
            resRoot.walkTopDown().filter { it.extension == "xml" }.forEach { file ->
                val xml = file.readText()
                Regex("@(\\w+)/(\\w+)").findAll(xml).forEach {
                    referenced += it.groupValues[1] to it.groupValues[2]
                }
                Regex("\\?attr/(\\w+)").findAll(xml).forEach {
                    referenced += "attr" to it.groupValues[1]
                }
            }
        }

        fun scanRClass() {
            val rFile = classesRoot.walkTopDown().find { it.name == "R.class" }
            if (rFile != null) {
                val rDir = rFile.parentFile
                rDir.listFiles { f -> f.isDirectory && f.name.startsWith("R$") }?.forEach { innerDir ->
                    val type = innerDir.name.substringAfter("R$")
                    innerDir.walkTopDown().filter { it.isFile && it.name.endsWith(".class") }.forEach { classFile ->
                        try {
                            val cr = ClassReader(classFile.readBytes())
                            cr.accept(object : ClassVisitor(Opcodes.ASM9) {
                                override fun visitField(access: Int, name: String, descriptor: String, sig: String?, value: Any?): FieldVisitor? {
                                    if (descriptor == "I") {
                                        declared += type to name
                                    }
                                    return super.visitField(access, name, descriptor, sig, value)
                                }
                            }, ClassReader.SKIP_FRAMES)
                        } catch (_: Exception) { }
                    }
                }
            }
        }

        fun scanManifest(manifestFile: File?) {
            if (manifestFile == null || !manifestFile.exists()) return
            val xml = manifestFile.readText()
            Regex("android:name=\"([^\"]+)\"").findAll(xml).forEach {
                val name = it.groupValues[1]
                if (!name.startsWith(".")) {
                    referencedClassesFromManifest += name.replace('.', '/')
                } else {
                    // relative: we could resolve package from manifest, skipped for brevity
                }
            }
        }

        // ---- Spring configuration files ----
        fun scanSpringConfigs(configDirs: List<File>) {
            if (!extension.scanConfigFiles) return
            configDirs.forEach { dir ->
                if (!dir.exists()) return@forEach
                dir.walkTopDown().forEach { file ->
                    when {
                        file.name.endsWith(".xml") -> {
                            val xml = file.readText()
                            // Find class attributes in bean definitions
                            Regex("class=\"([^\"]+)\"").findAll(xml).forEach {
                                val cls = it.groupValues[1]
                                referencedClassesFromSpring += cls.replace('.', '/')
                            }
                            // Also find factory-method, etc.
                        }
                        file.name.matches(Regex("application.*\\.(yml|yaml|properties)")) -> {
                            val content = file.readText()
                            // Simple heuristic: look for fully qualified class names
                            Regex("[a-zA-Z_][\\w.]*\\.[a-zA-Z_][\\w.]*").findAll(content).forEach {
                                val cls = it.value
                                if (cls.matches(Regex("^[a-z]+\\.[a-zA-Z_][\\w.]*$"))) {
                                    referencedClassesFromSpring += cls.replace('.', '/')
                                }
                            }
                        }
                    }
                }
            }
        }

        // Execute scans
        scanResources(mainResRoot)
        if (extension.includeTests) scanResources(testResRoot)
        scanRClass()
        val manifestFile = File(mainResRoot.parentFile, "AndroidManifest.xml")
        scanManifest(manifestFile)
        scanSpringConfigs(configRoots)

        return ResourceModel(
            declared = declared.toSet(),
            referenced = referenced.toSet(),
            referencedClassesFromManifest = referencedClassesFromManifest.toSet(),
            referencedClassesFromSpringConfig = referencedClassesFromSpring.toSet()
        )
    }
}