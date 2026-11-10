package dev.limebeck.templateEngine.parser

import dev.limebeck.templateEngine.SourceFile
import dev.limebeck.templateEngine.inputStream.InputStream
import dev.limebeck.templateEngine.inputStream.withFile

interface ParserError {
    val message: String
    val position: InputStream.Position
    val sourceFile: SourceFile?
        get() = position.sourceFile
}

internal inline fun <T> withSourceFile(file: SourceFile?, block: () -> T): T {
    if (file == null) return block()

    try {
        return block()
    } catch (error: LexerError) {
        throw if (error.sourceFile == null) error.copy(position = error.position.withFile(file), sourceFile = file)
        else error
    } catch (error: LanguageError) {
        throw if (error.sourceFile == null) error.copy(position = error.position.withFile(file), sourceFile = file)
        else error
    }
}
