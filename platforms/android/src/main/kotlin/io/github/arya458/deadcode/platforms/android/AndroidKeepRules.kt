package io.github.arya458.deadcode.platforms.android

import io.github.arya458.deadcode.core.model.ref.FieldRef
import io.github.arya458.deadcode.core.model.ref.MethodRef
import io.github.arya458.deadcode.core.platform.PlatformKeepRules

class AndroidKeepRules : PlatformKeepRules {

    private val lifecycleMethods = setOf(
        "onCreate", "onStart", "onResume", "onPause", "onStop", "onDestroy",
        "onCreateView", "onViewCreated", "onAttach", "onDetach"
    )

    private val componentTypes = setOf(
        "android/app/Activity",
        "androidx/appcompat/app/AppCompatActivity",
        "android/app/Service",
        "android/content/BroadcastReceiver",
        "androidx/fragment/app/Fragment",
        "androidx/lifecycle/ViewModel",
        "androidx/lifecycle/AndroidViewModel"
    )

    private val componentAnnotations = setOf(
        "android.app.Activity",
        "androidx.appcompat.app.AppCompatActivity",
        "android.app.Service",
        "android.content.BroadcastReceiver",
        "androidx.fragment.app.Fragment"
    )

    override fun shouldKeepMethod(
        method: MethodRef,
        classAnnotations: Map<String, Set<String>>
    ): Boolean {
        if (method.name in lifecycleMethods) {
            val ownerAnns = classAnnotations[method.owner] ?: emptySet()
            return ownerAnns.any { it in componentAnnotations }
        }
        return false
    }

    override fun shouldKeepField(field: FieldRef): Boolean = false

    override fun shouldKeepClass(
        className: String,
        classAnnotations: Map<String, Set<String>>
    ): Boolean {
        val anns = classAnnotations[className] ?: emptySet()
        return anns.any { it in componentAnnotations }
    }

    override fun shouldKeepResource(resourceType: String, resourceName: String): Boolean = false
}
