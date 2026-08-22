package io.github.arya458.deadcode.core.analyzer

import io.github.arya458.deadcode.core.model.ClassScanModel
import io.github.arya458.deadcode.core.model.DeadCodeModel
import io.github.arya458.deadcode.core.model.ResourceModel
import io.github.arya458.deadcode.core.platform.PlatformKeepRules
import org.objectweb.asm.Opcodes

data class AnalyzerConfig(
    val keepPublicApi: Boolean = true,
    val includeResources: Boolean = true,
    val keepAnnotations: Set<String> = emptySet(),
    val excludePackages: List<String> = emptyList(),
    val excludeClasses: List<String> = emptyList(),
    val excludeMethodNames: List<Regex> = emptyList(),
    val excludeFieldNames: List<Regex> = emptyList()
)

class DeadCodeAnalyzer(
    private val platformRules: PlatformKeepRules,
    private val config: AnalyzerConfig = AnalyzerConfig()
) {

    private val kotlinGeneratedMethodPatterns = listOf(
        Regex("component\\d+"),
        Regex("copy"),
        Regex("toString"),
        Regex("hashCode"),
        Regex("equals"),
        Regex("invoke"),
        Regex("get\\w+"),
        Regex("set\\w+")
    )

    fun analyze(classScan: ClassScanModel, resScan: ResourceModel): DeadCodeModel {
        val allReferencedMethods = classScan.referencedMethods + resScan.referencedMethodsFromXml

        val referencedResourcesFromFields = classScan.referencedFields.mapNotNull { field ->
            val owner = field.owner
            if (owner.contains("R$") && field.desc == "I") {
                val type = owner.substringAfterLast("R$")
                if (type.isNotEmpty()) type to field.name else null
            } else null
        }.toSet()

        val allReferencedResources = resScan.referenced + referencedResourcesFromFields
        val allReferencedClasses = classScan.referencedClasses +
            resScan.referencedClassesFromManifest +
            resScan.referencedClassesFromSpringConfig

        val deadMethods = classScan.declaredMethods.filter { method ->
            if (method.name in listOf("<init>", "<clinit>")) return@filter false
            if ((method.access and (Opcodes.ACC_SYNTHETIC or Opcodes.ACC_BRIDGE)) != 0) return@filter false
            if (kotlinGeneratedMethodPatterns.any { it.matches(method.name) }) return@filter false
            if (method.name == "main" &&
                (method.desc == "([Ljava/lang/String;)V" || method.desc == "()V")
            ) return@filter false
            if (config.keepPublicApi && (method.access and Opcodes.ACC_PUBLIC) != 0) return@filter false
            if (config.excludeMethodNames.any { it.matches(method.name) }) return@filter false
            if (hasKeepAnnotation(method.annotations)) return@filter false
            if (platformRules.shouldKeepMethod(method, classScan.classAnnotations)) return@filter false

            val directRef = allReferencedMethods.any {
                it.owner == method.owner && it.name == method.name && it.desc == method.desc
            }
            !directRef
        }

        val deadFields = classScan.declaredFields.filter { field ->
            if ((field.access and (Opcodes.ACC_SYNTHETIC or Opcodes.ACC_BRIDGE)) != 0) return@filter false
            if (config.keepPublicApi && (field.access and Opcodes.ACC_PUBLIC) != 0) return@filter false
            if (config.excludeFieldNames.any { it.matches(field.name) }) return@filter false
            if (hasKeepAnnotation(field.annotations)) return@filter false
            if (platformRules.shouldKeepField(field)) return@filter false

            classScan.referencedFields.none {
                it.owner == field.owner && it.name == field.name && it.desc == field.desc
            }
        }

        val deadClasses = classScan.declaredClasses.filter { cls ->
            val dotted = cls.replace('/', '.')
            if (config.excludePackages.any { dotted.startsWith(it) }) return@filter false
            if (config.excludeClasses.contains(dotted)) return@filter false
            if (cls == "kotlin/Metadata") return@filter false
            if (allReferencedClasses.contains(cls)) return@filter false
            if (classScan.declaredMethods.any {
                    it.owner == cls && it.name == "main" &&
                        (it.desc == "([Ljava/lang/String;)V" || it.desc == "()V")
                }
            ) return@filter false
            if (hasKeepAnnotation(classScan.classAnnotations[cls] ?: emptySet())) return@filter false
            if (platformRules.shouldKeepClass(cls, classScan.classAnnotations)) return@filter false

            val usedMember = classScan.declaredMethods.any { m ->
                m.owner == cls && allReferencedMethods.any { ref ->
                    ref.owner == m.owner && ref.name == m.name && ref.desc == m.desc
                }
            } || classScan.declaredFields.any { f ->
                f.owner == cls && classScan.referencedFields.any { ref ->
                    ref.owner == f.owner && ref.name == f.name && ref.desc == f.desc
                }
            }
            !usedMember
        }

        val deadResources = if (config.includeResources) {
            resScan.declared.filterNot { res ->
                allReferencedResources.contains(res) ||
                    platformRules.shouldKeepResource(res.first, res.second)
            }.toSet()
        } else emptySet()

        return DeadCodeModel(deadMethods, deadFields, deadClasses, deadResources)
    }

    private fun hasKeepAnnotation(annotations: Set<String>): Boolean =
        config.keepAnnotations.any { ann -> annotations.contains(ann) }
}
