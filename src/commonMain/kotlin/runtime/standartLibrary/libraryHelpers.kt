package dev.limebeck.templateEngine.runtime.standartLibrary

import dev.limebeck.templateEngine.runtime.KoteRuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeObject

fun List<RuntimeObject>.getAsString(index: Int) =
    getOrNull(index) as? RuntimeObject.StringWrapper
        ?: throw KoteRuntimeException("<64c14188> Expected string parameter at index $index")

fun List<RuntimeObject>.getAsNumber(index: Int) =
    getOrNull(index) as? RuntimeObject.NumberWrapper
        ?: throw KoteRuntimeException("<430153f8> Expected number parameter at index $index")