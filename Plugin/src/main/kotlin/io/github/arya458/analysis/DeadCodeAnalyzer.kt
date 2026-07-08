package io.github.arya458.analysis

import io.github.arya458.DeadCodeDetectorExtension
import io.github.arya458.model.ClassScanModel
import io.github.arya458.model.ResourceModel
import io.github.arya458.model.DeadCodeModel
import io.github.arya458.model.ref.FieldRef
import io.github.arya458.model.ref.MethodRef
import org.objectweb.asm.Opcodes

class DeadCodeAnalyzer(private val extension: DeadCodeDetectorExtension) {

    fun analyze(classScan: ClassScanModel, resScan: ResourceModel): DeadCodeModel {
        val (declaredMethods, declaredFields, declaredClasses, classAnnotations, referencedMethods, referencedFields, referencedClasses) = classScan
        val allReferencedClasses = referencedClasses +
                resScan.referencedClassesFromManifest +
                resScan.referencedClassesFromSpringConfig

        // Determine platform (auto-detect)
        val platform = detectPlatform(classAnnotations)

        // ---- Dead Methods ----
        val deadMethods = declaredMethods.filter { d ->
            if (d.name in listOf("<init>", "<clinit>")) return@filter false
            if ((d.access and Opcodes.ACC_SYNTHETIC) != 0) return@filter false
            if (extension.keepPublicApi && (d.access and Opcodes.ACC_PUBLIC) != 0) return@filter false
            if (extension.excludeMethods.any { it.matches(d.name) }) return@filter false
            if (hasKeepAnnotation(d.annotations)) return@filter false

            // Platform-specific keep rules
            if (shouldKeepForPlatform(d, platform, classAnnotations)) return@filter false

            val directRef = referencedMethods.any { it.owner == d.owner && it.name == d.name && it.desc == d.desc }
            !directRef
        }

        // ---- Dead Fields ----
        val deadFields = declaredFields.filter { f ->
            if ((f.access and Opcodes.ACC_SYNTHETIC) != 0) return@filter false
            if (extension.keepPublicApi && (f.access and Opcodes.ACC_PUBLIC) != 0) return@filter false
            if (extension.excludeFields.any { it.matches(f.name) }) return@filter false
            if (hasKeepAnnotation(f.annotations)) return@filter false
            if (shouldKeepFieldForPlatform(f, platform)) return@filter false
            referencedFields.none { it.owner == f.owner && it.name == f.name && it.desc == f.desc }
        }

        // ---- Dead Classes ----
        val deadClasses = declaredClasses.filter { cls ->
            if (extension.excludePackages.any { cls.replace('/', '.').startsWith(it) }) return@filter false
            if (extension.excludeClasses.contains(cls.replace('/', '.'))) return@filter false
            if (cls == "kotlin/Metadata") return@filter false
            if (allReferencedClasses.contains(cls)) return@filter false
            if (extension.keepPublicApi && isPublicClass(cls, classScan)) return@filter false
            if (hasKeepAnnotation(classAnnotations[cls] ?: emptySet())) return@filter false

            // Platform-specific class keep rules
            if (shouldKeepClassForPlatform(cls, classAnnotations, platform)) return@filter false

            // If any member of this class is used, keep it alive
            val usedMember = declaredMethods.any { it.owner == cls && referencedMethods.any { ref ->
                ref.owner == it.owner && ref.name == it.name && ref.desc == it.desc
            } } || declaredFields.any { it.owner == cls && referencedFields.any { ref ->
                ref.owner == it.owner && ref.name == it.name && ref.desc == it.desc
            } }
            !usedMember
        }

        // ---- Dead Resources ----
        val deadResources = if (extension.includeResources) resScan.declared - resScan.referenced else emptySet()

        return DeadCodeModel(deadMethods, deadFields, deadClasses, deadResources)
    }

    // ----- Helper functions -----

    private fun detectPlatform(classAnnotations: Map<String, Set<String>>): String {
        if (extension.platform != "auto") return extension.platform
        // Heuristic: if any class has Android annotations, treat as Android
        val androidAnn = setOf("android.app.Activity", "androidx.appcompat.app.AppCompatActivity", "android.service.Service")
        val springAnn = setOf("org.springframework.stereotype.Service", "org.springframework.stereotype.Controller", "org.springframework.web.bind.annotation.RestController")
        val allAnn = classAnnotations.values.flatten().toSet()
        return when {
            allAnn.any { androidAnn.contains(it) } -> "android"
            allAnn.any { springAnn.contains(it) } -> "spring"
            else -> "kmm" // default
        }
    }

    private fun hasKeepAnnotation(annotations: Set<String>): Boolean {
        return extension.keepAnnotations.any { ann -> annotations.contains(ann) }
    }

    private fun shouldKeepForPlatform(method: MethodRef, platform: String, classAnnotations: Map<String, Set<String>>): Boolean {
        return when (platform) {
            "spring" -> {
                // Keep methods with @RequestMapping, @GetMapping, @PostMapping, etc.
                val webAnnotations = setOf(
                    "org.springframework.web.bind.annotation.RequestMapping",
                    "org.springframework.web.bind.annotation.GetMapping",
                    "org.springframework.web.bind.annotation.PostMapping",
                    "org.springframework.web.bind.annotation.PutMapping",
                    "org.springframework.web.bind.annotation.DeleteMapping"
                )
                method.annotations.any { webAnnotations.contains(it) }
            }
            "android" -> {
                // Keep Android lifecycle methods (onCreate, onStart, etc.) only if in Activity/Service
                val lifecycle = setOf("onCreate", "onStart", "onResume", "onPause", "onStop", "onDestroy")
                if (method.name in lifecycle) {
                    // Check if owner class is an Android component
                    val ownerAnn = classAnnotations[method.owner] ?: emptySet()
                    val componentAnn = setOf("android.app.Activity", "androidx.appcompat.app.AppCompatActivity", "android.service.Service")
                    ownerAnn.any { componentAnn.contains(it) }
                } else false
            }
            "kmm" -> {
                // For KMM, keep expect/actual? Not implemented fully
                false
            }
            else -> false
        }
    }

    private fun shouldKeepFieldForPlatform(field: FieldRef, platform: String): Boolean {
        // Currently no special field keep rules
        return false
    }

    private fun shouldKeepClassForPlatform(cls: String, classAnnotations: Map<String, Set<String>>, platform: String): Boolean {
        val ann = classAnnotations[cls] ?: emptySet()
        return when (platform) {
            "spring" -> {
                val springAnn = setOf(
                    "org.springframework.stereotype.Service",
                    "org.springframework.stereotype.Controller",
                    "org.springframework.stereotype.Repository",
                    "org.springframework.stereotype.Component",
                    "org.springframework.web.bind.annotation.RestController",
                    "org.springframework.boot.autoconfigure.SpringBootApplication",
                    "org.springframework.context.annotation.Configuration"
                )
                ann.any { springAnn.contains(it) }
            }
            "android" -> {
                // Keep classes that are Activity, Service, BroadcastReceiver, etc.
                val androidComp = setOf(
                    "android.app.Activity",
                    "androidx.appcompat.app.AppCompatActivity",
                    "android.app.Service",
                    "android.content.BroadcastReceiver",
                    "androidx.fragment.app.Fragment"
                )
                ann.any { androidComp.contains(it) }
            }
            "kmm" -> {
                // For KMM, keep classes with @Shared or similar? Not yet.
                false
            }
            else -> false
        }
    }

    private fun isPublicClass(cls: String, classScan: ClassScanModel): Boolean {
        // Not implemented as we don't store class access; but can be extended.
        return false
    }
}