package com.aria.danesh.analysis

import com.aria.danesh.DeadCodeDetectorExtension
import com.aria.danesh.model.ClassScanModel
import com.aria.danesh.model.ref.FieldRef
import com.aria.danesh.model.ref.MethodRef
import org.objectweb.asm.*
import org.gradle.api.logging.Logger
import java.io.File

class ClassScanner() {

    fun scan(classesRoot: File, includeTests: Boolean): ClassScanModel {
        val declaredMethods = mutableSetOf<MethodRef>()
        val declaredFields = mutableSetOf<FieldRef>()
        val declaredClasses = mutableSetOf<String>()
        val referencedMethods = mutableSetOf<MethodRef>()
        val referencedFields = mutableSetOf<FieldRef>()
        val referencedClasses = mutableSetOf<String>()

        fun scanDir(dir: File) {
            if (!dir.exists()) return
            dir.walkTopDown().filter { it.isFile && it.extension == "class" }.forEach { file ->
                val cr = ClassReader(file.readBytes())
                cr.accept(object : ClassVisitor(Opcodes.ASM9) {
                    lateinit var currentClass: String
                    override fun visit(version: Int, access: Int, name: String, sig: String?, superName: String?, intf: Array<out String>?) {
                        currentClass = name
                        declaredClasses += name
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
                            }
                            override fun visitTypeInsn(op: Int, type: String) {
                                referencedClasses += type
                            }
                            override fun visitLdcInsn(value: Any) {
                                if (value is Type) referencedClasses += value.internalName
                            }
                        }
                    }
                }, ClassReader.SKIP_FRAMES)
            }
        }

        scanDir(classesRoot.resolve("java/main"))
        scanDir(classesRoot.resolve("kotlin/main"))
        if (includeTests) {
            scanDir(classesRoot.resolve("java/test"))
            scanDir(classesRoot.resolve("kotlin/test"))
        }

        return ClassScanModel(declaredMethods, declaredFields, declaredClasses, referencedMethods, referencedFields, referencedClasses)
    }
}
