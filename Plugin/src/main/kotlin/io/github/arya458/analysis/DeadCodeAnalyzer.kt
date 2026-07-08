package io.github.arya458.analysis

import io.github.arya458.DeadCodeDetectorExtension
import io.github.arya458.analysis.platform.*
import io.github.arya458.model.ClassScanModel
import io.github.arya458.model.ResourceModel
import io.github.arya458.model.DeadCodeModel
import io.github.arya458.model.ref.FieldRef
import io.github.arya458.model.ref.MethodRef
import org.objectweb.asm.Opcodes
import org.gradle.api.Project

class DeadCodeAnalyzer(
    private val project: Project,
    private val extension: DeadCodeDetectorExtension
) {

    private val platformRules: PlatformKeepRules by lazy {
        detectPlatformRules()
    }

    fun analyze(classScan: ClassScanModel, resScan: ResourceModel): DeadCodeModel {
        val (declaredMethods, declaredFields, declaredClasses, classAnnotations, referencedMethods, referencedFields, referencedClasses) = classScan

        val allReferencedClasses = referencedClasses +
                resScan.referencedClassesFromManifest +
                resScan.referencedClassesFromSpringConfig

        // ---- Dead Methods ----
        val deadMethods = declaredMethods.filter { method ->
            if (method.name in listOf("<init>", "<clinit>")) return@filter false
            if ((method.access and Opcodes.ACC_SYNTHETIC) != 0) return@filter false
            if (extension.keepPublicApi && (method.access and Opcodes.ACC_PUBLIC) != 0) return@filter false
            if (extension.excludeMethods.any { it.matcher(method.name).matches() }) return@filter false
            if (hasKeepAnnotation(method.annotations)) return@filter false
            if (platformRules.shouldKeepMethod(method, classAnnotations)) return@filter false
            if (extension.customKeepRules("method", classAnnotations, method)) return@filter false

            val directRef = referencedMethods.any { it.owner == method.owner && it.name == method.name && it.desc == method.desc }
            !directRef
        }

        // ---- Dead Fields ----
        val deadFields = declaredFields.filter { field ->
            if ((field.access and Opcodes.ACC_SYNTHETIC) != 0) return@filter false
            if (extension.keepPublicApi && (field.access and Opcodes.ACC_PUBLIC) != 0) return@filter false
            if (extension.excludeFields.any { it.matcher(field.name).matches() }) return@filter false
            if (hasKeepAnnotation(field.annotations)) return@filter false
            if (platformRules.shouldKeepField(field)) return@filter false
            if (extension.customKeepRules("field", emptyMap(), null)) return@filter false

            referencedFields.none { it.owner == field.owner && it.name == field.name && it.desc == field.desc }
        }

        // ---- Dead Classes ----
        val deadClasses = declaredClasses.filter { cls ->
            if (extension.excludePackages.any { cls.replace('/', '.').startsWith(it) }) return@filter false
            if (extension.excludeClasses.contains(cls.replace('/', '.'))) return@filter false
            if (cls == "kotlin/Metadata") return@filter false
            if (allReferencedClasses.contains(cls)) return@filter false
            if (extension.keepPublicApi && isPublicClass(cls, classScan)) return@filter false
            if (hasKeepAnnotation(classAnnotations[cls] ?: emptySet())) return@filter false
            if (platformRules.shouldKeepClass(cls, classAnnotations)) return@filter false
            if (extension.customKeepRules("class", classAnnotations, null)) return@filter false

            // اگر عضوی از این کلاس استفاده شده باشد، کلاس زنده است
            val usedMember = declaredMethods.any { it.owner == cls && referencedMethods.any { ref ->
                ref.owner == it.owner && ref.name == it.name && ref.desc == it.desc
            } } || declaredFields.any { it.owner == cls && referencedFields.any { ref ->
                ref.owner == it.owner && ref.name == it.name && ref.desc == it.desc
            } }
            !usedMember
        }

        val deadResources = if (extension.includeResources) {
            resScan.declared.filterNot { res ->
                resScan.referenced.contains(res) ||
                        platformRules.shouldKeepResource(res.first, res.second)
            }.toSet()
        } else emptySet()

        return DeadCodeModel(deadMethods, deadFields, deadClasses, deadResources)
    }

    private fun detectPlatformRules(): PlatformKeepRules {
        val platform = extension.platform
        if (platform != "auto") {
            return when (platform) {
                "android" -> AndroidKeepRules()
                "spring" -> SpringKeepRules()
                "kmm" -> KmmKeepRules()
                else -> KmmKeepRules()
            }
        }

        // Auto-detect بر اساس پلاگین‌های اعمال‌شده
        return when {
            project.plugins.hasPlugin("com.android.application") ||
                    project.plugins.hasPlugin("com.android.library") -> AndroidKeepRules()
            project.plugins.hasPlugin("org.springframework.boot") -> SpringKeepRules()
            else -> {
                // Heuristic: اگر annotationهای خاصی وجود داشته باشند
                // (این قسمت را می‌توان با اسکن annotationها قبل از این مرحله تکمیل کرد)
                KmmKeepRules()
            }
        }
    }

    private fun hasKeepAnnotation(annotations: Set<String>): Boolean {
        return extension.keepAnnotations.any { ann -> annotations.contains(ann) }
    }

    private fun isPublicClass(cls: String, classScan: ClassScanModel): Boolean {
        // برای پیاده‌سازی کامل نیاز به ذخیره‌سازی access modifier کلاس داریم، که در حال حاضر در مدل نیست.
        // فعلاً false برمی‌گردانیم.
        return false
    }
}