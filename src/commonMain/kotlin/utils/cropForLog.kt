package dev.limebeck.templateEngine.utls

inline fun String.cropForLog(size: Int = 100) = if (size <= length) {
    substring(0, size) + "..."
} else this