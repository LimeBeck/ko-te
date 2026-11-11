import dev.limebeck.templateEngine.inputStream.isNextSequenceEquals
import dev.limebeck.templateEngine.inputStream.toStream
import kotlin.test.*

class InputStreamTest {
    @Test
    fun delimiterLookaheadDoesNotRewindTheCharacterStream() {
        val stream = "prefix\n{{".toStream()
        stream.seek(7)
        val position = stream.currentPosition

        assertTrue(stream.isNextSequenceEquals(listOf('{', '{')))
        assertSame(position, stream.currentPosition)
        assertFalse(stream.isNextSequenceEquals(listOf('{', '}')))
        assertSame(position, stream.currentPosition)
        assertFalse(stream.isNextSequenceEquals(listOf('{', '{', '{')))
        assertSame(position, stream.currentPosition)
        assertEquals('{', stream.next())
    }

    @Test
    fun streamFromString() {
        val string = """
            val variable = "Value"
            val anotherVariable = functionCall(variable)
        """.trimIndent()
        val stream = string.toStream()

        assertTrue("Stream has next value") { stream.hasNext() }

        stream.debug()

        assertEquals('v', stream.peek())
        assertEquals('v', stream.peek())
        assertEquals('v', stream.next())
        assertEquals('a', stream.peek())
        assertEquals('a', stream.next())
        assertEquals(1, stream.currentPosition.line)
        assertEquals(2, stream.currentPosition.absolutePosition)

        stream.debug()

        for(char in stream){
            assertNotNull(char)
        }
        assertFalse("End of stream must be reached") { stream.hasNext() }

        stream.seek(0)

        assertTrue("Must be rewinded to start") { stream.hasNext() }
        assertEquals('v', stream.peek())

        stream.seek(23)
        assertEquals('v', stream.peek())
    }

    @Test
    fun positioningTest() {
        val stream = """
            123
            456
        """.trimIndent().toStream()
        assertEquals(4, stream.getPositionAtLineStart(2))

        stream.seek(4)

        assertEquals('4', stream.peek())
    }
}
