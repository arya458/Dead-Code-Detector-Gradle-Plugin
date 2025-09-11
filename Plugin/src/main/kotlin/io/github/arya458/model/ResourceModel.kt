package io.github.arya458.model

data class ResourceModel(
    val declared: Set<Pair<String, String>>,
    val referenced: Set<Pair<String, String>>
)