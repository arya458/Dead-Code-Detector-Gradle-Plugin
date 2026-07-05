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

    fun scan(mainResRoot: File, testResRoot: File, classesRoot: File): ResourceModel {
        val declared = ConcurrentSkipListSet<Pair<String, String>>()
        val referenced = ConcurrentSkipListSet<Pair<String, String>>()
        val referencedClassesFromManifest = ConcurrentSkipListSet<String>()

        // 1. Scan resource XML files
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
            // Extract @type/name references
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

        // 2. Scan generated R class
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
                                override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?) {
                                    // no-op
                                }
                                override fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?): FieldVisitor? {
                                    if (descriptor == "I") {
                                        declared += type to name
                                    }
                                    return super.visitField(access, name, descriptor, signature, value)
                                }
                            }, ClassReader.SKIP_FRAMES)
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                }
            }
        }

        // 3. Scan AndroidManifest.xml
        fun scanManifest(manifestFile: File?) {
            if (manifestFile == null || !manifestFile.exists()) return
            val xml = manifestFile.readText()
            Regex("android:name=\"([^\"]+)\"").findAll(xml).forEach {
                val name = it.groupValues[1]
                if (!name.startsWith(".")) {
                    referencedClassesFromManifest += name.replace('.', '/')
                }
            }
        }

        scanResources(mainResRoot)
        if (extension.includeTests) scanResources(testResRoot)
        scanRClass()
        val manifestFile = File(mainResRoot.parentFile, "AndroidManifest.xml")
        scanManifest(manifestFile)

        return ResourceModel(
            declared = declared.toSet(),
            referenced = referenced.toSet(),
            referencedClassesFromManifest = referencedClassesFromManifest.toSet()
        )
    }
}