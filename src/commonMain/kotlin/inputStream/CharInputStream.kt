package dev.limebeck.templateEngine.inputStream

import dev.limebeck.templateEngine.utls.cropForLog

class CharInputStream(private val string: String) : RewindableInputStream<Char> {
    private var position: StringPosition = StringPosition(
        absolutePosition = 0,
        line = 1,
        column = 0
    )

    private val stringLines by lazy {
        string.split('\n')
    }

    override fun peek(): Char {
        return string[position.absolutePosition]
    }

    override fun next(): Char {
        val pos = position.absolutePosition
        val char = string[pos]

        position = if (char == '\n') {
            position.copy(
                line = position.line + 1,
                column = 0,
                absolutePosition = pos + 1
            )
        } else {
            position.copy(
                absolutePosition = pos + 1,
                column = position.column + 1,
            )
        }

        return char
    }

    fun getPositionAtLineStart(line: Int): Int {
        var positionAtLineStart = 0
        for (lineNumber in 0 until line - 1) {
            positionAtLineStart += stringLines[lineNumber].length + 1
        }
        return positionAtLineStart
    }

    override fun seek(absolutePosition: Int) {
        stringLines.foldIndexed(0) { index, positionAtLineStart, string ->
            if (absolutePosition !in positionAtLineStart..(positionAtLineStart + string.length)) {
                positionAtLineStart + string.length + 1
            } else {
                position = StringPosition(
                    absolutePosition = absolutePosition,
                    line = index + 1, //Index starts from 0, but lines - from 1
                    column = absolutePosition - positionAtLineStart
                )

                return
            }
        }
    }

    override fun hasNext(): Boolean =
        string.length != position.absolutePosition + 1

    override val currentPosition: StringPosition
        get() = position

    data class StringPosition(
        override val absolutePosition: Int,
        val line: Int,
        val column: Int
    ) : InputStream.Position

    override fun toString(): String {
        return "CharInputStream(${this.hashCode()}) on position $position for string ${string.cropForLog()}"
    }

    fun debug() {
        stringLines.forEachIndexed { index, line ->
            println("$line ### length:${line.length}, start at: ${getPositionAtLineStart(index + 1)}")
            if (index + 1 == position.line)
                println("_".repeat(position.column) + "^ is next char")
        }
    }
}

fun String.toStream() = CharInputStream(this)