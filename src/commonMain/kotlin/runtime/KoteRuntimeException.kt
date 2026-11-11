package dev.limebeck.templateEngine.runtime

import dev.limebeck.templateEngine.inputStream.InputStream
import dev.limebeck.templateEngine.inputStream.formatDiagnostic

data class KoteRuntimeException(
    val reason: String? = null,
    val underlyingException: Throwable? = null,
    val position: InputStream.Position? = null
) : Throwable(reason?.let { position?.formatDiagnostic(it) ?: it }, underlyingException) {
    val sourceFile
        get() = position?.sourceFile
}

internal inline fun <T> InputStream.Position.withRuntimeLocation(block: () -> T): T {
    try {
        return block()
    } catch (error: KoteRuntimeException) {
        if (error.position != null) throw error
        throw error.copy(position = this)
    }
}
