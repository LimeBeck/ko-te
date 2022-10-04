package dev.limebeck.templateEngine.runtime.standartLibrary

val stringUtils = listOf(
    lowercase,
    uppercase
)

val std = arrayOf(
    *stringUtils.toTypedArray(),
    kote
)