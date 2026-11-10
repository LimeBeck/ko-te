package dev.limebeck.templateEngine

data class SourceFile(
    val path: String,
    val name: String = path.substringAfterLast('/').substringAfterLast('\\')
)
