package io.github.arya458.analysis

import io.github.arya458.model.ClassScanModel
import io.github.arya458.model.ref.FieldRef
import io.github.arya458.model.ref.MethodRef
import org.objectweb.asm.*
import java.io.File
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.stream.Stream

class ClassScanner(private val parallel: Boolean = true) {

    fun scan(classesRoot: File, includeTests: Boolean): ClassScanModel {
        val declaredMethods = ConcurrentSkipListSet<MethodRef>()
        val declaredFields = ConcurrentSkipListSet<FieldRef>()
        val declaredClasses = ConcurrentSkipListSet<String>()
        val classAnnotations = ConcurrentSkipListMap<String, MutableSet<String>>()
        val referencedMethods = ConcurrentSkipListSet<MethodRef>()
        val referencedFields = ConcurrentSkipListSet<FieldRef>()
        val referencedClasses = ConcurrentSkipListSet<String>()

        fun scanDir(dir: File) {
            if (!dir.exists()) return
            val classFiles = dir.walkTopDown()
                .filter { it.isFile && it.extension == "class" }
                .toList()

            val stream: Stream<File> = if (parallel) classFiles.parallelStream() else classFiles.stream()
            stream.forEach { file ->
                try {
                    val cr = ClassReader(file.readBytes())
                    var currentClass = ""
                    cr.accept(object : ClassVisitor(Opcodes.ASM9) {
                        override fun visit(version: Int, access: Int, name: String, sig: String?, superName: String?, intf: Array<out String>?) {
                            currentClass = name
                            declaredClasses += name
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
                                    declaredFields += FieldRef(currentClass, name, desc, access, fieldAnns)
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
                                    referencedMethods += MethodRef(owner, name, desc)
                                    referencedClasses += owner
                                }

                                override fun visitFieldInsn(op: Int, owner: String, name: String, desc: String) {
                                    referencedFields += FieldRef(owner, name, desc)
                                    referencedClasses += owner
                                }

                                override fun visitTypeInsn(op: Int, type: String) {
                                    referencedClasses += type
                                }

                                override fun visitLdcInsn(value: Any) {
                                    when (value) {
                                        is Type -> referencedClasses += value.internalName
                                        is String -> {
                                            // optional heuristic – ignored
                                        }
                                    }
                                }

                                override fun visitEnd() {
                                    declaredMethods += MethodRef(currentClass, name, desc, access, methodAnns)
                                    super.visitEnd()
                                }
                            }
                        }
                    }, ClassReader.SKIP_FRAMES)
                } catch (e: Exception) {
                    System.err.println("Error scanning ${file.absolutePath}: ${e.message}")
                }
            }
        }

        // Scan main and test source folders (including KMM)
        listOf("java/main", "kotlin/main", "commonMain", "androidMain", "iosMain").forEach { dir ->
            scanDir(classesRoot.resolve(dir))
        }
        if (includeTests) {
            listOf("java/test", "kotlin/test", "commonTest", "androidTest", "iosTest").forEach { dir ->
                scanDir(classesRoot.resolve(dir))
            }
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
}