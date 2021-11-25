package dev.limebeck.templateEngine.parser

import dev.limebeck.templateEngine.inputStream.InputStream

interface ParserError {
    val message: String
    val position: InputStream.Position
}