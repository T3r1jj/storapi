package io.github.t3r1jj.storapi.upstream

import io.github.t3r1jj.storapi.data.Record
import io.github.t3r1jj.storapi.data.RecordMeta
import io.github.t3r1jj.storapi.data.exception.StorageException
import io.github.t3r1jj.storapi.factory.DefaultUpstreamStorageFactory
import io.github.t3r1jj.storapi.factory.UpstreamStorageFactory
import org.apache.commons.io.IOUtils
import org.junit.After
import org.junit.Before
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

@RunWith(Parameterized::class)
class UpstreamStorageIT(private val factory: UpstreamStorageFactory<UpstreamStorage>) {

    lateinit var uploadedRecord: RecordMeta
    lateinit var storage: UpstreamStorage

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data() = DefaultUpstreamStorageFactory.listOf(
                Megaupload::class,
                Openload::class,
                Put::class,

                ForumFiles::class,
                AnonFile::class,
                BayFiles::class,
                FileBz::class,
                UploadSt::class
        )
    }

    @Before
    fun setUp() {
        storage = factory.createStorage()
    }

    @After
    fun tearDown() {
        factory.asCleanable(storage)?.let {
            if (uploadedRecord.id != null) {
                it.delete(uploadedRecord)
            } else {
                uploadedRecord.id = ""
                assertFailsWith(StorageException::class) {
                    it.delete(uploadedRecord)
                }
            }
        }
    }

    @org.junit.Test
    fun upload() {
        val name = System.currentTimeMillis().toString()
        val record = Record("$name.tmp", "", "Some text".byteInputStream())
        uploadedRecord = storage.upload(record)
        assertEquals(record.name, uploadedRecord.name)
        assertTrue(uploadedRecord.name.isNotBlank())
        assertTrue(uploadedRecord.publicPath != null);
    }

    @org.junit.Test
    fun download() {
        val name = System.currentTimeMillis().toString()
        val data = "Some text"
        val record = Record("$name.tmp", "", data.byteInputStream())
        val unreadRecord = record.copy(data = data.byteInputStream())
        uploadedRecord = storage.upload(record)
        Thread.sleep(100)
        val downloadedRecord = storage.download(uploadedRecord.path)
        assertTrue(downloadedRecord.path.isNotBlank())
        assertEquals(unreadRecord.name, downloadedRecord.name)
        assertTrue(IOUtils.contentEquals(unreadRecord.data, downloadedRecord.data))
    }

    @org.junit.Test
    fun isPresent() {
        val name = System.currentTimeMillis().toString()
        val record = Record("$name.tmp", "", "Some text".byteInputStream())
        uploadedRecord = storage.upload(record)
        assertTrue(storage.isPresent(uploadedRecord.path))
    }

    @org.junit.Test
    fun isNotPresent() {
        val name = System.currentTimeMillis().toString()
        uploadedRecord = RecordMeta("", "", 0L)
        if (storage.toString().toLowerCase() == "put") {
            assertFalse(storage.isPresent("https://s.put.re/$name.tmp"))
        } else {
            assertFalse(storage.isPresent("/$name/something"))
        }
    }
}