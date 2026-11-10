package dev.limebeck.templateEngine.parser

import dev.limebeck.templateEngine.SourceFile
import dev.limebeck.templateEngine.inputStream.InputStream
import dev.limebeck.templateEngine.inputStream.StreamError
import dev.limebeck.templateEngine.inputStream.formatDiagnostic
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
        if (error.sourceFile != null) throw error
        throw lexerError(error.position.withFile(file), error.message)
    } catch (error: LanguageError) {
        if (error.sourceFile != null) throw error
        throw languageError(error.message, error.position.withFile(file))
    } catch (error: StreamError) {
        if (error.position.sourceFile != null) throw error
        val position = error.position.withFile(file)
        throw error.copy(position = position, message = position.formatDiagnostic(error.message))
    }
}

internal fun lexerError(position: InputStream.Position, message: String): LexerError =
    LexerError(position, position.formatDiagnostic(message))

internal fun languageError(message: String, position: InputStream.Position): LanguageError =
    LanguageError(position.formatDiagnostic(message), position)
