package io.github.arya458.model.ref

import java.io.Serializable

data class MethodRef(
    val owner: String,
    val name: String,
    val desc: String,
    val access: Int = 0,
    val annotations: Set<String> = emptySet()
) : Serializable