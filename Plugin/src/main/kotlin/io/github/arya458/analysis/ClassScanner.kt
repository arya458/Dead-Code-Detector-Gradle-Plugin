package io.github.arya458.analysis

import io.github.arya458.model.ClassScanModel
import io.github.arya458.model.ref.FieldRef
import io.github.arya458.model.ref.MethodRef
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.objectweb.asm.*
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap

class ClassScanner(
    private val project: Project,
    private val parallel: Boolean = true,
    private val enableCaching: Boolean = true
) {

    fun scan(includeTests: Boolean, includeOnlyPackages: List<String> = emptyList()): ClassScanModel {
        val cacheFile = project.layout.buildDirectory.file("dead-code-cache/class-scan.ser").get().asFile
        if (enableCaching && cacheFile.exists()) {
            try {
                val lastModified = cacheFile.lastModified()
                val classDirs = getClassDirectories(includeTests)
                val allClassFiles = classDirs.flatMap { it.walkTopDown().filter { f -> f.isFile && f.extension == "class" }.toList() }
                val latestChange = allClassFiles.maxOfOrNull { it.lastModified() } ?: 0L
                if (latestChange <= lastModified) {
                    return ObjectInputStream(Files.newInputStream(cacheFile.toPath())).use { ois ->
                        ois.readObject() as ClassScanModel
                    }
                }
            } catch (e: Exception) {
                project.logger.warn("Could not read cache, rescanning...")
            }
        }

        val declaredMethods = ConcurrentHashMap.newKeySet<MethodRef>()
        val declaredFields = ConcurrentHashMap.newKeySet<FieldRef>()
        val declaredClasses = ConcurrentHashMap.newKeySet<String>()
        val referencedMethods = ConcurrentHashMap.newKeySet<MethodRef>()
        val referencedFields = ConcurrentHashMap.newKeySet<FieldRef>()
        val referencedClasses = ConcurrentHashMap.newKeySet<String>()
        val classAnnotations = ConcurrentHashMap<String, MutableSet<String>>()

        val dirs = getClassDirectories(includeTests)

        dirs.forEach { dir ->
            processDirectory(dir, declaredMethods, declaredFields, declaredClasses, classAnnotations,
                referencedMethods, referencedFields, referencedClasses, includeOnlyPackages)
        }

        val result = ClassScanModel(
            declaredMethods = declaredMethods.toSet(),
            declaredFields = declaredFields.toSet(),
            declaredClasses = declaredClasses.toSet(),
            classAnnotations = classAnnotations.mapValues { it.value.toSet() },
            referencedMethods = referencedMethods.toSet(),
            referencedFields = referencedFields.toSet(),
            referencedClasses = referencedClasses.toSet()
        )

        // ذخیره در کش
        if (enableCaching) {
            try {
                cacheFile.parentFile.mkdirs()
                ObjectOutputStream(Files.newOutputStream(cacheFile.toPath())).use { oos ->
                    oos.writeObject(result)
                }
            } catch (e: Exception) {
                project.logger.warn("Could not write cache: ${e.message}")
            }
        }

        return result
    }

    private fun getClassDirectories(includeTests: Boolean): List<File> {
        val sourceSets = project.extensions.findByType(SourceSetContainer::class.java)
        val allDirs = mutableSetOf<File>()

        if (sourceSets != null) {
            allDirs.addAll(sourceSets.getByName("main").output.classesDirs.files)
            if (includeTests) {
                allDirs.addAll(sourceSets.getByName("test").output.classesDirs.files)
            }

            project.rootProject.subprojects.forEach { sub ->
                val subSourceSets = sub.extensions.findByType(SourceSetContainer::class.java)
                if (subSourceSets != null) {
                    allDirs.addAll(subSourceSets.getByName("main").output.classesDirs.files)
                    if (includeTests) {
                        allDirs.addAll(subSourceSets.getByName("test").output.classesDirs.files)
                    }
                }
            }
        } else {
            // Fallback
            val mainDir = project.layout.buildDirectory.dir("classes/java/main").get().asFile
            val testDir = project.layout.buildDirectory.dir("classes/java/test").get().asFile
            allDirs.add(mainDir)
            if (includeTests) allDirs.add(testDir)
        }

        return allDirs.filter { it.exists() }
    }

    private fun processDirectory(
        dir: File,
        declaredMethods: MutableSet<MethodRef>,
        declaredFields: MutableSet<FieldRef>,
        declaredClasses: MutableSet<String>,
        classAnnotations: MutableMap<String, MutableSet<String>>,
        referencedMethods: MutableSet<MethodRef>,
        referencedFields: MutableSet<FieldRef>,
        referencedClasses: MutableSet<String>,
        includeOnlyPackages: List<String>
    ) {
        val classFiles = dir.walkTopDown()
            .filter { it.isFile && it.extension == "class" }
            .toList()

        val stream = if (parallel) classFiles.parallelStream() else classFiles.stream()
        stream.forEach { file ->
            try {
                val cr = ClassReader(file.readBytes())
                var currentClass = ""
                cr.accept(object : ClassVisitor(Opcodes.ASM9) {
                    override fun visit(version: Int, access: Int, name: String, sig: String?, superName: String?, intf: Array<out String>?) {
                        currentClass = name
                        val pkg = name.substringBeforeLast('/').replace('/', '.')
                        if (includeOnlyPackages.isNotEmpty() && includeOnlyPackages.none { pkg.startsWith(it) }) {
                            return
                        }
                        declaredClasses.add(name)
                        classAnnotations.computeIfAbsent(name) { mutableSetOf() }
                    }

                    override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
                        val annName = desc.substring(1, desc.length - 1).replace('/', '.')
                        classAnnotations[currentClass]?.add(annName)
                        return super.visitAnnotation(desc, visible)
                    }

                    override fun visitField(access: Int, name: String, desc: String, sig: String?, value: Any?): FieldVisitor? {
                        val fieldAnns = mutableSetOf<String>()
                        val visitor = super.visitField(access, name, desc, sig, value)
                        return object : FieldVisitor(Opcodes.ASM9, visitor) {
                            override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
                                val annName = desc.substring(1, desc.length - 1).replace('/', '.')
                                fieldAnns.add(annName)
                                return super.visitAnnotation(desc, visible)
                            }
                            override fun visitEnd() {
                                declaredFields.add(FieldRef(currentClass, name, desc, access, fieldAnns))
                                super.visitEnd()
                            }
                        }
                    }

                    override fun visitMethod(access: Int, name: String, desc: String, sig: String?, ex: Array<out String>?): MethodVisitor {
                        val methodAnns = mutableSetOf<String>()
                        val visitor = super.visitMethod(access, name, desc, sig, ex)
                        return object : MethodVisitor(Opcodes.ASM9, visitor) {
                            override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
                                val annName = desc.substring(1, desc.length - 1).replace('/', '.')
                                methodAnns.add(annName)
                                return super.visitAnnotation(desc, visible)
                            }

                            override fun visitMethodInsn(op: Int, owner: String, name: String, desc: String, isIntf: Boolean) {
                                referencedMethods.add(MethodRef(owner, name, desc))
                                referencedClasses.add(owner)
                            }

                            override fun visitFieldInsn(op: Int, owner: String, name: String, desc: String) {
                                referencedFields.add(FieldRef(owner, name, desc))
                                referencedClasses.add(owner)
                            }

                            override fun visitTypeInsn(op: Int, type: String) {
                                referencedClasses.add(type)
                            }

                            override fun visitLdcInsn(value: Any) {
                                if (value is Type) {
                                    referencedClasses.add(value.internalName)
                                }
                            }

                            override fun visitEnd() {
                                declaredMethods.add(MethodRef(currentClass, name, desc, access, methodAnns))
                                super.visitEnd()
                            }
                        }
                    }
                }, ClassReader.SKIP_FRAMES)
            } catch (e: Exception) {
                project.logger.error("Error scanning ${file.absolutePath}: ${e.message}")
            }
        }
    }
}