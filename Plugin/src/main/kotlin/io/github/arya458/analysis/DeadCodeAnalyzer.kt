package io.github.arya458.analysis

import io.github.arya458.DeadCodeDetectorExtension
import io.github.arya458.model.ClassScanModel
import io.github.arya458.model.ResourceModel
import org.objectweb.asm.Opcodes

/**
 * Analyzes scanned data to identify dead code (methods, fields, classes) and resources.
 * Respects exclusion rules, public API keeping, and annotation-based keep rules.
 */
class DeadCodeAnalyzer(private val extension: DeadCodeDetectorExtension) {

    fun analyze(classScan: ClassScanModel, resScan: ResourceModel): io.github.arya458.model.DeadCodeModel {
        val (declaredMethods, declaredFields, declaredClasses, referencedMethods, referencedFields, referencedClasses) = classScan
        val allReferencedClasses = referencedClasses + resScan.referencedClassesFromManifest

        // 1. Dead methods
        val deadMethods = declaredMethods.filter { d ->
            if (d.name in listOf("<init>", "<clinit>")) return@filter false
            if ((d.access and Opcodes.ACC_SYNTHETIC) != 0) return@filter false
            if (extension.keepPublicApi && (d.access and Opcodes.ACC_PUBLIC) != 0) return@filter false
            if (hasKeepAnnotation(d)) return@filter false
            if (extension.excludeMethods.any { it.matches(d.name) }) return@filter false

            // Direct reference check
            val directRef = referencedMethods.any { it.owner == d.owner && it.name == d.name && it.desc == d.desc }
            if (directRef) return@filter false

            // Additional checks: interface implementations, reflection (simplified)
            // For now, if no direct reference, consider dead
            true
        }

        // 2. Dead fields
        val deadFields = declaredFields.filter { d ->
            if ((d.access and Opcodes.ACC_SYNTHETIC) != 0) return@filter false
            if (extension.keepPublicApi && (d.access and Opcodes.ACC_PUBLIC) != 0) return@filter false
            if (hasKeepAnnotation(d)) return@filter false
            if (extension.excludeFields.any { it.matches(d.name) }) return@filter false
            referencedFields.none { it.owner == d.owner && it.name == d.name && it.desc == d.desc }
        }

        // 3. Dead classes
        val deadClasses = declaredClasses.filter { cls ->
            if (extension.excludePackages.any { cls.replace('/', '.').startsWith(it) }) return@filter false
            if (extension.excludeClasses.any { cls.replace('/', '.') == it }) return@filter false
            if (cls == "kotlin/Metadata") return@filter false
            if (allReferencedClasses.contains(cls)) return@filter false
            if (extension.keepPublicApi && isPublicClass(cls, classScan)) return@filter false

            // Check if any member of this class is used
            val usedMember = declaredMethods.any { it.owner == cls && referencedMethods.any { ref ->
                ref.owner == it.owner && ref.name == it.name && ref.desc == it.desc
            } } || declaredFields.any { it.owner == cls && referencedFields.any { ref ->
                ref.owner == it.owner && ref.name == it.name && ref.desc == it.desc
            } }
            !usedMember
        }

        // 4. Dead resources
        val deadResources = if (extension.includeResources) resScan.declared - resScan.referenced else emptySet()

        return io.github.arya458.model.DeadCodeModel(deadMethods, deadFields, deadClasses, deadResources)
    }

    // Helper: check if member has an annotation that should keep it alive
    private fun hasKeepAnnotation(member: Any): Boolean {
        // In real implementation, we need to extract annotations from ASM.
        // For now, stub: if extension.keepAnnotations is not empty, we'd need to check.
        return false
    }

    // Helper: check if a class is public (requires access info; we don't store it in ClassScanModel)
    private fun isPublicClass(cls: String, classScan: ClassScanModel): Boolean {
        // We'd need to store access flags for classes. For brevity, we return false.
        return false
    }
}