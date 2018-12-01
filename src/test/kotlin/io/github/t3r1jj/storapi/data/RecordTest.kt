package io.github.t3r1jj.storapi.data

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals


class RecordTest {

    @Test
    fun testEquals() {
        val record1 = Record("a", "b", "1".byteInputStream())
        val record2 = Record("a", "b", "1".byteInputStream())
        assertEquals(record1, record2)
    }

    @Test
    fun testNotEqualsDifferentData() {
        val record1 = Record("a", "b", "1".byteInputStream())
        val record2 = Record("a", "b", "2".byteInputStream())
        assertNotEquals(record1, record2)
    }

    @Test
    fun testNotEqualsDifferentName() {
        val record1 = Record("a", "b", "1".byteInputStream())
        val record2 = Record("B", "b", "1".byteInputStream())
        assertNotEquals(record1, record2)
    }

    @Test
    fun testNotEqualsDifferentPath() {
        val record1 = Record("a", "b", "1".byteInputStream())
        val record2 = Record("a", "C", "1".byteInputStream())
        assertNotEquals(record1, record2)
    }

    @Test
    fun testHashCode() {
        val record1 = Record("a", "b", "1".byteInputStream())
        val record2 = Record("a", "b", "1".byteInputStream())
        assertEquals(record1.hashCode(), record2.hashCode())
    }

    @Test
    fun testHashCodeDifferentPath() {
        val record1 = Record("a", "b", "1".byteInputStream())
        val record2 = Record("a", "c", "1".byteInputStream())
        assertNotEquals(record1.hashCode(), record2.hashCode())
    }

    @Test
    fun testHashCodeDifferentNameAndData() {
        val record1 = Record("a", "b", "1".byteInputStream())
        val record2 = Record("D", "b", "2".byteInputStream())
        assertEquals(record1.hashCode(), record2.hashCode())
    }
}