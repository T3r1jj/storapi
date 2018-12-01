package io.github.t3r1jj.storapi.authenticated.mega

import com.github.eliux.mega.cmd.AbstractMegaCmd
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MegaCmdTest {
    private val stringUnderTest = "d---    -          - 29Oct2018 18:24:52 io.github.t3r1jj.fcms.external"

    @Test
    fun testReplace() {
        val string = java.lang.String(stringUnderTest)
        val string2 = stringUnderTest
        val words = string.split("\\s+")
        val words2 = string2.split("\\s+")
        assertEquals(6, words.size)
        assertNotEquals(words.size, words2.size)
    }

    @Test
    fun trimSplitOfCmdDu() {
        val cmd = MegaCmdDu()
        val words = invokeTrimSplit(cmd, stringUnderTest)
        assertEquals(6, words.size)
    }

    @Test
    fun trimSplitOfCmdRecursiveList() {
        val cmd = MegaCmdRecursiveList("")
        val words = invokeTrimSplit(cmd, stringUnderTest)
        assertEquals(6, words.size)
    }

    private fun invokeTrimSplit(instance: AbstractMegaCmd<out Any>, param: String): Array<out String> {
        val method = instance.javaClass.getDeclaredMethod("trimSplit", String::class.java)
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return method.invoke(instance, param) as Array<out String>
    }
}