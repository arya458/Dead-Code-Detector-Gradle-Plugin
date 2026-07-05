package io.github.arya458.analysis

import io.github.arya458.model.ClassScanModel
import io.github.arya458.model.ref.FieldRef
import io.github.arya458.model.ref.MethodRef
import org.objectweb.asm.*
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet

/**
 * Scans compiled class files to extract declared and referenced members.
 * Supports parallel scanning for performance and catches exceptions per file.
 */
class ClassScanner {

    fun scan(classesRoot: File, includeTests: Boolean): ClassScanModel {
        // Thread-safe sets for concurrent scanning
        val declaredMethods = ConcurrentSkipListSet<MethodRef>()
        val declaredFields = ConcurrentSkipListSet<FieldRef>()
        val declaredClasses = ConcurrentSkipListSet<String>()
        val referencedMethods = ConcurrentSkipListSet<MethodRef>()
        val referencedFields = ConcurrentSkipListSet<FieldRef>()
        val referencedClasses = ConcurrentSkipListSet<String>()

        fun scanDir(dir: File) {
            if (!dir.exists()) return
            val classFiles = dir.walkTopDown()
                .filter { it.isFile && it.extension == "class" }
                .toList()

            // Parallel processing using Java 8 parallelStream
            classFiles.parallelStream().forEach { file ->
                try {
                    val cr = ClassReader(file.readBytes())
                    var currentClass = ""
                    cr.accept(object : ClassVisitor(Opcodes.ASM9) {
                        override fun visit(version: Int, access: Int, name: String, sig: String?, superName: String?, intf: Array<out String>?) {
                            currentClass = name
                            declaredClasses += name
                            // Note: R class handling is done in ResourceScanner, but we can also detect fields here
                        }

                        override fun visitField(access: Int, name: String, desc: String, sig: String?, value: Any?): FieldVisitor? {
                            declaredFields += FieldRef(currentClass, name, desc, access)
                            return super.visitField(access, name, desc, sig, value)
                        }

                        override fun visitMethod(access: Int, name: String, desc: String, sig: String?, ex: Array<out String>?): MethodVisitor {
                            val mref = MethodRef(currentClass, name, desc, access)
                            declaredMethods += mref
                            return object : MethodVisitor(Opcodes.ASM9) {
                                override fun visitMethodInsn(op: Int, owner: String, name: String, desc: String, isIntf: Boolean) {
                                    referencedMethods += MethodRef(owner, name, desc)
                                    referencedClasses += owner
                                }

                                override fun visitFieldInsn(op: Int, owner: String, name: String, desc: String) {
                                    referencedFields += FieldRef(owner, name, desc)
                                    referencedClasses += owner
                                    // If owner is R or R$*, treat as resource reference (handled later)
                                }

                                override fun visitTypeInsn(op: Int, type: String) {
                                    referencedClasses += type
                                }

                                override fun visitLdcInsn(value: Any) {
                                    when (value) {
                                        is Type -> {
                                            referencedClasses += value.internalName
                                            // Could be used for Class.forName detection
                                        }
                                        is String -> {
                                            // Simple heuristic: if string looks like a fully qualified class name
                                            if (value.matches(Regex("^[a-zA-Z_][\\w.]*$"))) {
                                                // Potential class name, but we avoid false positives
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }, ClassReader.SKIP_FRAMES)
                } catch (e: Exception) {
                    System.err.println("Error scanning class file: ${file.absolutePath} - ${e.message}")
                }
            }
        }

        // Scan main and test source directories
        scanDir(classesRoot.resolve("java/main"))
        scanDir(classesRoot.resolve("kotlin/main"))
        if (includeTests) {
            scanDir(classesRoot.resolve("java/test"))
            scanDir(classesRoot.resolve("kotlin/test"))
        }

        return ClassScanModel(
            declaredMethods = declaredMethods.toSet(),
            declaredFields = declaredFields.toSet(),
            declaredClasses = declaredClasses.toSet(),
            referencedMethods = referencedMethods.toSet(),
            referencedFields = referencedFields.toSet(),
            referencedClasses = referencedClasses.toSet()
        )
    }
}