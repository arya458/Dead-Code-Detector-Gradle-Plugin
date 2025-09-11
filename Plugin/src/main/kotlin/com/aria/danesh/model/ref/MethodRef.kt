package com.aria.danesh.model.ref

data class MethodRef(val owner: String, val name: String, val desc: String, val access: Int = 0)
