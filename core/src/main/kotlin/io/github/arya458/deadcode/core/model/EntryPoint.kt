package io.github.arya458.deadcode.core.model

sealed class EntryPoint {
    data class Method(val owner: String, val name: String, val desc: String) : EntryPoint()
    data class Class(val name: String) : EntryPoint()
    data class Resource(val type: String, val name: String) : EntryPoint()
}
