package dev.limebeck.templateEngine.utils

import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.RuntimeObject
import dev.limebeck.templateEngine.runtime.wrap
import kotlin.reflect.KProperty

typealias KoteFunctionDefinition = Pair<String, RuntimeObject.CallableWrapper>
typealias KoteFunction = (args: List<RuntimeObject>, context: RuntimeContext) -> Any

fun koteFunction(name: String, block: KoteFunction): KoteFunctionDefinition {
    return name to RuntimeObject.CallableWrapper { args: List<RuntimeObject>, context: RuntimeContext ->
        block(args, context).wrap()
    }
}

class KoteFunctionDefinitionDelegate(
    val block: KoteFunction
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): KoteFunctionDefinition {
        return property.name to RuntimeObject.CallableWrapper { args: List<RuntimeObject>, context: RuntimeContext ->
            block(args, context).wrap()
        }
    }
}

fun koteFun(block: KoteFunction) = KoteFunctionDefinitionDelegate(block)