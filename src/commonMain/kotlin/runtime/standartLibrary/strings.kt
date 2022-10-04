package dev.limebeck.templateEngine.runtime.standartLibrary

import dev.limebeck.templateEngine.utils.koteFun

val lowercase by koteFun { args, context ->
    val value = args.getAsString(0)
    return@koteFun value.string.lowercase()
}

val uppercase by koteFun { args, context ->
    val value = args.getAsString(0)
    return@koteFun value.string.uppercase()
}