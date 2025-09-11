package com.aria.danesh.analysis

import com.aria.danesh.DeadCodeDetectorExtension
import com.aria.danesh.model.ClassScanModel
import com.aria.danesh.model.DeadCodeModel
import com.aria.danesh.model.ResourceModel
import org.objectweb.asm.Opcodes

class DeadCodeAnalyzer(private val extension: DeadCodeDetectorExtension) {

    fun analyze(classScan: ClassScanModel, resScan: ResourceModel): DeadCodeModel {
        val (declaredMethods, declaredFields, declaredClasses, referencedMethods, referencedFields, referencedClasses) = classScan
        val deadMethods = declaredMethods.filter { d ->
            if (d.name in listOf("<init>", "<clinit>")) return@filter false
            if ((d.access and Opcodes.ACC_SYNTHETIC) != 0) return@filter false
            referencedMethods.none { it.owner == d.owner && it.name == d.name && it.desc == d.desc }
        }
        val deadFields = declaredFields.filter { d ->
            if ((d.access and Opcodes.ACC_SYNTHETIC) != 0) return@filter false
            referencedFields.none { it.owner == d.owner && it.name == d.name && it.desc == d.desc }
        }
        val deadClasses = declaredClasses.filter { cls ->
            if (extension.excludePackages.any { cls.replace('/', '.').startsWith(it) }) return@filter false
            if (cls == "kotlin/Metadata") return@filter false
            if (referencedClasses.contains(cls)) return@filter false
            val usedMember = declaredMethods.any { it.owner == cls && referencedMethods.any { ref ->
                ref.owner == it.owner && ref.name == it.name && ref.desc == it.desc
            } }
            !usedMember
        }
        val deadResources = if (extension.includeResources) resScan.declared - resScan.referenced else emptySet()
        return DeadCodeModel(deadMethods, deadFields, deadClasses, deadResources)
    }
}
