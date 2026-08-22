package io.github.arya458.deadcode.core.model.ref

data class MethodRef(
    val owner: String,
    val name: String,
    val desc: String,
    val access: Int = 0,
    val annotations: Set<String> = emptySet()
) : java.io.Serializable
