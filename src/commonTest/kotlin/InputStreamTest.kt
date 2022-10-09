import dev.limebeck.templateEngine.inputStream.toStream
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.iterator.shouldHaveNext
import io.kotest.matchers.iterator.shouldNotHaveNext
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class InputStreamTest : FunSpec ({
    test("Stream from string") {
        val string = """
            val variable = "Value"
            val anotherVariable = functionCall(variable)
        """.trimIndent()
        val stream = string.toStream()

        withClue("Stream has next value"){
            stream.hasNext() shouldBe true
        }
        stream.peek() shouldBe 'v'
        stream.peek() shouldBe 'v'
        stream.next() shouldBe 'v'

        stream.peek() shouldBe 'a'
        stream.next() shouldBe 'a'

        stream.currentPosition.line shouldBe 1
        stream.currentPosition.absolutePosition shouldBe 2

        for (char in stream) {
            char.shouldNotBeNull()
        }
        withClue("End of stream must be reached"){
            stream.shouldNotHaveNext()
        }

        stream.seek(0)

        withClue("Must be rewinded to start"){
            stream.shouldHaveNext()
            stream.peek() shouldBe 'v'
            stream.currentPosition.absolutePosition shouldBe 0
        }

        withClue("Rewind to position 23") {
            stream.seek(23)
            stream.peek() shouldBe 'v'
        }
    }

    test("Stream positioning") {
        val stream = """
            123
            456
        """.trimIndent().toStream()
        stream.getPositionAtLineStart(2) shouldBe 4

        stream.seek(4)

        stream.peek() shouldBe '4'
    }
})