package dev.limebeck.templateEngine

import dev.limebeck.templateEngine.parser.ParserError

typealias JsonObject = Map<String, Any>

class Renderer {
    fun render(template: String, resources: List<Resource<Any>>?, data: JsonObject): Result<String, ParserError> {
        return TODO()
    }
}

interface Resource<T> {
    val path: String
    val name: String
    val content: T
}



