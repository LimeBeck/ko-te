package dev.limebeck.templateEngine.runtime

import dev.limebeck.templateEngine.Result
import dev.limebeck.templateEngine.parser.ParserError

interface Renderer {
    suspend fun render(templateName: String, context: RuntimeContext): Result<String, ParserError>
}