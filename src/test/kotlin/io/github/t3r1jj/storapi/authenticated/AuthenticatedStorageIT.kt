package io.github.t3r1jj.storapi.authenticated

import io.github.t3r1jj.storapi.data.Record
import io.github.t3r1jj.storapi.data.RecordMeta
import io.github.t3r1jj.storapi.data.exception.StorageException
import io.github.t3r1jj.storapi.factory.DropboxFactory
import io.github.t3r1jj.storapi.factory.GoogleDriveFactory
import io.github.t3r1jj.storapi.factory.MegaFactory
import io.github.t3r1jj.storapi.factory.StorageFactory
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.math.BigInteger
import java.util.*
import java.util.function.Consumer
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(Parameterized::class)
class AuthenticatedStorageIT(private val factory: StorageFactory<AuthenticatedStorage>) {
    private lateinit var storageWithoutAccess: AuthenticatedStorage
    private lateinit var storage: AuthenticatedStorage

    companion object {
        private val testRootPath = "/" + this::class.java.`package`.name
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data() = listOf(
                DropboxFactory(), GoogleDriveFactory(), MegaFactory()
        )
    }

    @Before
    fun setUp() {
        storage = factory.createStorage()
        storageWithoutAccess = factory.createStorageWithoutAccess()
    }

    @After
    fun tearDown() {
        storage.login()
        storage.findAll("")
                .filter { it.path.contains(testRootPath) }
                .forEach { storage.delete(RecordMeta("", it.path, 0)) }
        storage.logout()
    }

    @org.junit.Test
    fun testIsNotLogged() {
        assertFalse(storageWithoutAccess.isLogged())
    }

    @org.junit.Test
    fun testLoginWrongCredentials() {
        assertFailsWith(StorageException::class) {
            storageWithoutAccess.login()
        }
    }

    @org.junit.Test
    fun testIsLogged() {
        storage.login()
        assertTrue(storage.isLogged())
    }

    @org.junit.Test
    fun testUpload() {
        val name = System.currentTimeMillis().toString()
        val record = Record("$name.tmp", "$testRootPath/$name.tmp", "Some text".byteInputStream())
        storage.login()
        storage.upload(record)
        assertTrue(storage.isPresent(record.path))
    }

    @org.junit.Test
    fun testUploadProgress() {
        val progressResults = mutableListOf<Long>()
        val name = System.currentTimeMillis().toString()
        val size = 1024 * 1024
        val chars = CharArray(size)
        Arrays.fill(chars, 'a')
        val oneMBText = String(chars)
        val record = Record("$name.tmp", "$testRootPath/$name.tmp", oneMBText.byteInputStream())
        storage.login()
        val progressListener = Consumer<Long> { bytesWritten ->
            progressResults.add(bytesWritten)
            println("WRITTEN: $bytesWritten bytes of $size (${bytesWritten.toDouble() * 100 / size}%), finished: " + (bytesWritten == size.toLong()))
        }
        storage.upload(record, progressListener)
        assertTrue(storage.isPresent(record.path))
        Thread.sleep(1001)
        assertTrue(progressResults.size > 0)
    }

    @org.junit.Test
    fun testUploadOverride() {
        val name = System.currentTimeMillis().toString()
        val samePath = "$testRootPath/$name.tmp"
        val text = "Some text"
        val differentText = "Some text2"
        val record = Record("$name.tmp", samePath, text.byteInputStream())
        val record2 = Record("$name.tmp", samePath, differentText.byteInputStream())
        val record2Unread = record2.copy(data = differentText.byteInputStream())
        storage.login()
        storage.upload(record)
        storage.upload(record2)
        val uploadedRecord = storage.download(record2.path)
        assertEquals(record2Unread, uploadedRecord)
    }

    @org.junit.Test
    fun testFindAll() {
        val name = System.currentTimeMillis().toString()
        val text = "Some text"
        val record = Record("$name.tmp", "$testRootPath/$name.tmp", text.byteInputStream())
        storage.login()
        storage.upload(record)
        MatcherAssert.assertThat(storage.findAll(""), Matchers.hasItem(RecordMeta(record.name, record.path, text.length.toLong())))
    }

    @org.junit.Test
    fun testDownload() {
        val name = System.currentTimeMillis().toString()
        val text = "Some text"
        val record = Record("$name.tmp", "$testRootPath/$name.tmp", text.byteInputStream())
        val unreadRecord = record.copy(data = text.byteInputStream())
        storage.login()
        storage.upload(record)
        val storedRecord = storage.download(record.path)
        assertEquals(unreadRecord, storedRecord)
    }

    @org.junit.Test
    fun testDownloadProgress() {
        val progressResults = mutableListOf<Long>()
        val name = System.currentTimeMillis().toString()
        val size = 1024 * 1024
        val chars = CharArray(size)
        Arrays.fill(chars, 'a')
        val oneMBText = String(chars)
        val record = Record("$name.tmp", "$testRootPath/$name.tmp", oneMBText.byteInputStream())
        val unreadRecord = record.copy(data = oneMBText.byteInputStream())
        storage.login()
        storage.upload(record)
        val progressListener = Consumer<Long> { bytesWritten ->
            progressResults.add(bytesWritten)
            println("WRITTEN: $bytesWritten bytes of $size (${bytesWritten.toDouble() * 100 / size}%), finished: " + (bytesWritten == size.toLong()))
        }
        val storedRecord = storage.download(record.path, progressListener)
        assertEquals(unreadRecord, storedRecord)
        Thread.sleep(1001)
        assertTrue(progressResults.size > 0)
    }

    @org.junit.Test
    fun testDelete() {
        val name = System.currentTimeMillis().toString()
        val record = Record("$name.tmp", "$testRootPath/$name.tmp", "Some text".byteInputStream())
        val meta = RecordMeta("", record.path, 0L)
        storage.login()
        storage.upload(record)
        storage.delete(meta)
        assertFalse(storage.isPresent(record.path))
    }

    @org.junit.Test
    fun testIsNotPresent() {
        val record = Record("test.txt", "/XXXXXXXXXXXXXXXXXXXX", "Some text".byteInputStream())
        storage.login()
        assertFalse(storage.isPresent(record.path))
    }

    @org.junit.Test
    fun testIsNotPresentInvalidPath() {
        val record = Record("test.txt", "(*&@()*!*IJUDE wjiasddj", "Some text".byteInputStream())
        storage.login()
        assertFalse(storage.isPresent(record.path))
    }

    @org.junit.Test
    fun testGetInfo() {
        storage.login()
        val info = storage.getInfo()
        MatcherAssert.assertThat(info.name.toLowerCase(), Matchers.containsString(storage.toString().toLowerCase()))
        MatcherAssert.assertThat(info.totalSpace, Matchers.greaterThan(BigInteger.ZERO))
    }

    @org.junit.Test
    fun testGetInfoSpaceUsed() {
        val name = System.currentTimeMillis().toString()
        val record = Record("$name.tmp", "$testRootPath/$name.tmp", "Some text".byteInputStream())
        storage.login()
        storage.upload(record)
        val info = storage.getInfo()
        MatcherAssert.assertThat(info.usedSpace, Matchers.greaterThan(BigInteger.ZERO))
        MatcherAssert.assertThat(info.totalSpace, Matchers.greaterThan(info.usedSpace))
    }

    @org.junit.Test
    fun testLogout() {
        storage.login()
        storage.logout()
        assertFalse(storage.isLogged())
    }
}