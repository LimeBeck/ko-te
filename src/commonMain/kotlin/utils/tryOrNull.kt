package dev.limebeck.templateEngine.utils

inline fun <T> tryOrNull(block: () -> T): T? = try {
    block()
} catch (t: Throwable) {
    null
}