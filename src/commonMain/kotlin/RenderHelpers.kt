package dev.limebeck.templateEngine

import dev.limebeck.templateEngine.runtime.KoteRuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeObject

fun List<RuntimeObject>.render() = joinToString("") { render(it) }

private fun quoteJson(value: String): String = buildString {
    append('"')
    value.forEach { char ->
        when (char) {
            '"' -> append("\\\"")
            '\\' -> append("\\\\")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            '\b' -> append("\\b")
            '\u000C' -> append("\\f")
            else -> if (char.code < 0x20 || char.code in 0xD800..0xDFFF) {
                append("\\u").append(char.code.toString(16).padStart(4, '0'))
            } else append(char)
        }
    }
    append('"')
}

private fun renderJson(value: RuntimeObject): String = when (value) {
    is RuntimeObject.StringWrapper -> quoteJson(value.string)
    RuntimeObject.Null -> "null"
    is RuntimeObject.BooleanWrapper -> value.value.toString()
    is RuntimeObject.NumberWrapper -> {
        val number = value.number
        if ((number !is Byte && number !is Short && number !is Int && number !is Long &&
                number !is Float && number !is Double) || !number.toDouble().isFinite()) {
            throw KoteRuntimeException("Cannot serialize a non-finite or unsupported number")
        }
        number.toString()
    }
    is RuntimeObject.CollectionWrapper -> value.collection.joinToString(",", "[", "]") { renderJson(it) }
    is RuntimeObject.ObjectWrapper -> value.objectMap.entries.joinToString(",", "{", "}") {
        "${quoteJson(it.key)}:${renderJson(it.value)}"
    }
    else -> throw KoteRuntimeException("Cannot serialize a callable or Nothing as JSON")
}

fun renderObjectToJson(objectWrapper: RuntimeObject.ObjectWrapper): String = renderJson(objectWrapper)

fun render(value: RuntimeObject): String = when (value) {
    is RuntimeObject.StringWrapper -> value.string
    RuntimeObject.Nothing -> ""
    is RuntimeObject.CallableWrapper -> throw KoteRuntimeException("Cannot render a callable; call it first")
    else -> renderJson(value)
}
