package com.aria.danesh.model

import com.aria.danesh.model.ref.FieldRef
import com.aria.danesh.model.ref.MethodRef


data class ClassScanModel(
    val declaredMethods: Set<MethodRef>,
    val declaredFields: Set<FieldRef>,
    val declaredClasses: Set<String>,
    val referencedMethods: Set<MethodRef>,
    val referencedFields: Set<FieldRef>,
    val referencedClasses: Set<String>
)