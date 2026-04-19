package com.analyticskit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class EventTest {

    @Test
    fun `event creation with name and properties`() {
        val event = Event(
            name = "button_clicked",
            properties = mapOf("button" to "checkout", "price" to 29.99)
        )

        assertEquals("button_clicked", event.name)
        assertEquals("checkout", event.properties["button"])
        assertEquals(29.99, event.properties["price"])
    }

    @Test
    fun `event creation with default properties`() {
        val event = Event(name = "page_viewed")

        assertEquals("page_viewed", event.name)
        assertEquals(emptyMap<String, Any>(), event.properties)
        assertNotEquals(0L, event.timestamp)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `event with blank name throws`() {
        Event(name = "")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `event with whitespace name throws`() {
        Event(name = "   ")
    }
}
