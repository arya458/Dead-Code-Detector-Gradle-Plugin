package io.github.arya458.analysis

import io.github.arya458.model.ClassScanModel
import io.github.arya458.model.ref.FieldRef
import io.github.arya458.model.ref.MethodRef
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.objectweb.asm.*
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.ConcurrentSkipListMap

class ClassScanner(
    private val project: Project,
    private val parallel: Boolean = true
) {

    fun scan(includeTests: Boolean): ClassScanModel {
        val declaredMethods = ConcurrentSkipListSet<MethodRef>()
        val declaredFields = ConcurrentSkipListSet<FieldRef>()
        val declaredClasses = ConcurrentSkipListSet<String>()
        val classAnnotations = ConcurrentSkipListMap<String, MutableSet<String>>()
        val referencedMethods = ConcurrentSkipListSet<MethodRef>()
        val referencedFields = ConcurrentSkipListSet<FieldRef>()
        val referencedClasses = ConcurrentSkipListSet<String>()

        val sourceSets = project.extensions.findByType(SourceSetContainer::class.java)
        val dirs = if (sourceSets != null) {
            val main = sourceSets.getByName("main").output.classesDirs.files
            val test = if (includeTests) sourceSets.getByName("test").output.classesDirs.files else emptySet()
            (main + test).filter { it.exists() }
        } else {
            project.logger.warn("SourceSetContainer not found, using default build/classes directories")
            val mainDir = project.layout.buildDirectory.dir("classes/java/main").get().asFile
            val testDir = project.layout.buildDirectory.dir("classes/java/test").get().asFile
            listOf(mainDir).plus(if (includeTests) listOf(testDir) else emptyList()).filter { it.exists() }
        }

        dirs.forEach { dir ->
            processDirectory(dir, declaredMethods, declaredFields, declaredClasses, classAnnotations,
                referencedMethods, referencedFields, referencedClasses)
        }

        return ClassScanModel(
            declaredMethods = declaredMethods.toSet(),
            declaredFields = declaredFields.toSet(),
            declaredClasses = declaredClasses.toSet(),
            classAnnotations = classAnnotations.mapValues { it.value.toSet() },
            referencedMethods = referencedMethods.toSet(),
            referencedFields = referencedFields.toSet(),
            referencedClasses = referencedClasses.toSet()
        )
    }

    private fun processDirectory(
        dir: File,
        declaredMethods: MutableSet<MethodRef>,
        declaredFields: MutableSet<FieldRef>,
        declaredClasses: MutableSet<String>,
        classAnnotations: MutableMap<String, MutableSet<String>>,
        referencedMethods: MutableSet<MethodRef>,
        referencedFields: MutableSet<FieldRef>,
        referencedClasses: MutableSet<String>
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