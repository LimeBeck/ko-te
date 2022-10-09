package dev.limebeck.templateEngine

import dev.limebeck.templateEngine.runtime.RuntimeObject


//TODO: Move to SimpleRenderer object
fun List<RuntimeObject>.render() = joinToString("") { render(it) }

fun renderObjectToJson(objectWrapper: RuntimeObject.ObjectWrapper): String {
    return """{ ${
        objectWrapper.objectMap.entries.joinToString(",") {
            val (key, value) = it
            """ "$key": ${if (value is RuntimeObject.StringWrapper) "\"${value.string}\"" else render(value)}  """
        }
    } }"""
}

fun render(value: RuntimeObject): String {
    return when (value) {
        is RuntimeObject.StringWrapper -> value.string
        is RuntimeObject.Null -> "NULL"
        is RuntimeObject.NumberWrapper -> value.number.toString()
        is RuntimeObject.BooleanWrapper -> value.value.toString()
        is RuntimeObject.CollectionWrapper -> "[${
            value.collection.joinToString(",") {
                if (it is RuntimeObject.StringWrapper) {
                    "\"${it.string}\""
                } else {
                    render(it)
                }
            }
        }]"

        is RuntimeObject.ObjectWrapper -> renderObjectToJson(value)
        is RuntimeObject.CallableWrapper -> "TODO"
        RuntimeObject.Nothing -> ""
    }
}
