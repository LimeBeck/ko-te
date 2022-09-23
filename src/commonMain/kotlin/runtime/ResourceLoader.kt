package dev.limebeck.templateEngine.runtime

interface ResourceLoader {
    fun loadResource(identifier: String): Resource
}

interface Resource {
    val identifier: String
    val content: ByteArray
    val contentType: String
}

class StaticResourceLoader(
    val resources: List<Resource> = emptyList()
) : ResourceLoader {
    override fun loadResource(identifier: String): Resource =
        resources.find { it.identifier == identifier }
            ?: throw KoteRuntimeException("<16cf57ee> Unable to load resource with identifier $identifier")
}