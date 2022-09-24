package dev.limebeck.templateEngine.utils

import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.RuntimeObject
import dev.limebeck.templateEngine.runtime.wrap

fun koteFunction(name: String, block: (args: List<RuntimeObject>, context: RuntimeContext) -> Any): Pair<String, RuntimeObject.CallableWrapper> {
    return name to RuntimeObject.CallableWrapper { args: List<RuntimeObject>, context: RuntimeContext ->
        block(args, context).wrap()
    }
}