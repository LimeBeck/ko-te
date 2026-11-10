package dev.limebeck.templateEngine.parser

import dev.limebeck.templateEngine.SourceFile
import dev.limebeck.templateEngine.inputStream.InputStream

interface ParserError {
    val message: String
    val position: InputStream.Position
    val sourceFile: SourceFile?
        get() = null
}

internal inline fun <T> withSourceFile(file: SourceFile?, block: () -> T): T {
    if (file == null) return block()

    try {
        return block()
    } catch (error: LexerError) {
        throw if (error.sourceFile == null) error.copy(sourceFile = file) else error
    } catch (error: LanguageError) {
        throw if (error.sourceFile == null) error.copy(sourceFile = file) else error
    }
}
