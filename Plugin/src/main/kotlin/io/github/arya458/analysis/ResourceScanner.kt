package io.github.arya458.analysis

import io.github.arya458.DeadCodeDetectorExtension
import io.github.arya458.model.ResourceModel
import java.io.File

class ResourceScanner(private val extension: DeadCodeDetectorExtension) {

    fun scan(mainResRoot: File, testResRoot: File): ResourceModel {
        val declared = mutableSetOf<Pair<String, String>>()
        val referenced = mutableSetOf<Pair<String, String>>()

        fun scanResources(resRoot: File) {
            if (!extension.includeResources || !resRoot.exists()) return
            resRoot.walkTopDown().forEach { file ->
                if (!file.isFile) return@forEach
                val type = file.parentFile.name
                val name = file.nameWithoutExtension
                when (type) {
                    "layout", "drawable", "mipmap", "menu", "raw" -> declared += type to name
                    "values" -> if (file.extension == "xml") {
                        val xml = file.readText()
                        Regex("<(string|color|dimen|style) name=\"(.*?)\"")
                            .findAll(xml).forEach { declared += it.groupValues[1] to it.groupValues[2] }
                    }
                }
            }
            resRoot.walkTopDown().filter { it.extension == "xml" }.forEach { file ->
                val xml = file.readText()
                Regex("@(\\w+)/(\\w+)").findAll(xml).forEach {
                    referenced += it.groupValues[1] to it.groupValues[2]
                }
            }
        }

        scanResources(mainResRoot)
        if (extension.includeTests) scanResources(testResRoot)

        return ResourceModel(declared, referenced)
    }
}
