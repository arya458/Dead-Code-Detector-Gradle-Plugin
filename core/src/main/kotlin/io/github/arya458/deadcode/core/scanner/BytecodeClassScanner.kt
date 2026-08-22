package io.github.arya458.deadcode.core.scanner

import io.github.arya458.deadcode.core.model.ClassScanModel
import io.github.arya458.deadcode.core.model.ref.FieldRef
import io.github.arya458.deadcode.core.model.ref.MethodRef
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Pure bytecode scanner – no Gradle dependency.
 */
class BytecodeClassScanner(
    private val parallel: Boolean = true
) {

    fun scan(
        classDirectories: List<File>,
        includeOnlyPackages: List<String> = emptyList()
    ): ClassScanModel {
        val declaredMethods = ConcurrentHashMap.newKeySet<MethodRef>()
        val declaredFields = ConcurrentHashMap.newKeySet<FieldRef>()
        val declaredClasses = ConcurrentHashMap.newKeySet<String>()
        val referencedMethods = ConcurrentHashMap.newKeySet<MethodRef>()
        val referencedFields = ConcurrentHashMap.newKeySet<FieldRef>()
        val referencedClasses = ConcurrentHashMap.newKeySet<String>()
        val classAnnotations = ConcurrentHashMap<String, MutableSet<String>>()

        classDirectories.filter { it.exists() }.forEach { dir ->
            processDirectory(
                dir,
                declaredMethods,
                declaredFields,
                declaredClasses,
                classAnnotations,
                referencedMethods,
                referencedFields,
                referencedClasses,
                includeOnlyPackages
            )
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
                    override fun visit(
                        version: Int,
                        access: Int,
                        name: String,
                        sig: String?,
                        superName: String?,
                        intf: Array<out String>?
                    ) {
                        currentClass = name
                        val pkg = name.substringBeforeLast('/').replace('/', '.')
                        if (includeOnlyPackages.isNotEmpty() &&
                            includeOnlyPackages.none { pkg.startsWith(it) }
                        ) {
                            return
                        }
                        declaredClasses.add(name)
                        classAnnotations.computeIfAbsent(name) { mutableSetOf() }
                    }

                    override fun visitAnnotation(desc: String, visible: Boolean) =
                        super.visitAnnotation(desc, visible).also {
                            val annName = desc.substring(1, desc.length - 1).replace('/', '.')
                            classAnnotations[currentClass]?.add(annName)
                        }

                    override fun visitField(
                        access: Int,
                        name: String,
                        desc: String,
                        sig: String?,
                        value: Any?
                    ): FieldVisitor {
                        val fieldAnns = mutableSetOf<String>()
                        val visitor = super.visitField(access, name, desc, sig, value)
                        return object : FieldVisitor(Opcodes.ASM9, visitor) {
                            override fun visitAnnotation(desc: String, visible: Boolean) =
                                super.visitAnnotation(desc, visible).also {
                                    fieldAnns.add(desc.substring(1, desc.length - 1).replace('/', '.'))
                                }

                            override fun visitEnd() {
                                declaredFields.add(FieldRef(currentClass, name, desc, access, fieldAnns))
                                super.visitEnd()
                            }
                        }
                    }

                    override fun visitMethod(
                        access: Int,
                        name: String,
                        desc: String,
                        sig: String?,
                        ex: Array<out String>?
                    ): MethodVisitor {
                        val methodAnns = mutableSetOf<String>()
                        val visitor = super.visitMethod(access, name, desc, sig, ex)
                        return object : MethodVisitor(Opcodes.ASM9, visitor) {
                            override fun visitAnnotation(desc: String, visible: Boolean) =
                                super.visitAnnotation(desc, visible).also {
                                    methodAnns.add(desc.substring(1, desc.length - 1).replace('/', '.'))
                                }

                            override fun visitMethodInsn(
                                op: Int,
                                owner: String,
                                name: String,
                                desc: String,
                                isIntf: Boolean
                            ) {
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
                                declaredMethods.add(
                                    MethodRef(currentClass, name, desc, access, methodAnns)
                                )
                                super.visitEnd()
                            }
                        }
                    }
                }, ClassReader.SKIP_FRAMES)
            } catch (_: Exception) {
                // skip unreadable class files
            }
        }
    }
}
