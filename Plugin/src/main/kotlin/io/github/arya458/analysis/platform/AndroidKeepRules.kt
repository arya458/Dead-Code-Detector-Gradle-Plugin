package io.github.arya458.analysis.platform

import io.github.arya458.model.ref.MethodRef
import io.github.arya458.model.ref.FieldRef
import org.objectweb.asm.Opcodes

class AndroidKeepRules : PlatformKeepRules {
    private val lifecycleMethods = setOf("onCreate", "onStart", "onResume", "onPause", "onStop", "onDestroy")
    private val componentAnnotations = setOf(
        "android.app.Activity",
        "androidx.appcompat.app.AppCompatActivity",
        "android.app.Service",
        "android.content.BroadcastReceiver",
        "androidx.fragment.app.Fragment"
    )

    override fun shouldKeepMethod(method: MethodRef, classAnnotations: Map<String, Set<String>>): Boolean {
        if (method.name in lifecycleMethods) {
            val ownerAnns = classAnnotations[method.owner] ?: emptySet()
            return ownerAnns.any { componentAnnotations.contains(it) }
        }
        return false
    }

    override fun shouldKeepField(field: FieldRef): Boolean = false

    override fun shouldKeepClass(className: String, classAnnotations: Map<String, Set<String>>): Boolean {
        val anns = classAnnotations[className] ?: emptySet()
        return anns.any { componentAnnotations.contains(it) }
    }

    override fun shouldKeepResource(resourceType: String, resourceName: String): Boolean = false
}